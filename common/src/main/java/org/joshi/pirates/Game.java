package org.joshi.pirates;

import org.joshi.pirates.cards.CardDeck;
import org.joshi.pirates.cards.FortuneCard;

import java.util.ArrayList;

/**
 * This class represents a single game.
 */
public class Game {

    private static final int MAX_SCORE = 3000;

    private final int MAX_PLAYERS;

    private final ArrayList<Player> players;

    int currentPlayer = 0;

    PlayerId winner = null;

    /**
     * Identifier of the player who has crossed winning number of points.
     */
    int finalPlayer = -1;

    /**
     * The current card.
     */
    private FortuneCard currentCard = null;

    private final CardDeck cardDeck = new CardDeck();

    public Game(int maxPlayers) {
        MAX_PLAYERS = maxPlayers;
        players = new ArrayList<>(MAX_PLAYERS);
        cardDeck.shuffle();
    }

    public Game() {
        this(3);
    }

    /**
     * Method to add player to this game.
     *
     * @param player the player to add
     */
    public void addPlayer(Player player) {
        players.add(player);
    }

    /**
     * Method that indicates if the game can start.
     *
     * @return true if it can start
     */
    public boolean canPlay() {
        return players.size() == MAX_PLAYERS;
    }

    /**
     * Start turn of the player
     *
     * @return player id of the player whose turn to start
     */
    public PlayerId startTurn() {
        if (cardDeck.isEmpty()) {
            cardDeck.shuffle();
        }
        currentCard = cardDeck.top();
        return players.get(currentPlayer).getPlayerId();
    }

    public void endTurn(TurnResult result) {
        if (result.islandOfDead()) {
            // Player was on island of skulls, update other player scores
            for (int i = 0; i < MAX_PLAYERS; i++) {
                if (i != currentPlayer) {
                    players.get(i).addScore(result.score());
                }
            }
        } else {
            players.get(currentPlayer).addScore(result.score());
            if (finalPlayer == -1 && players.get(currentPlayer).getScore() >= MAX_SCORE) {
                finalPlayer = currentPlayer;
            }
        }

        currentPlayer++;
        if (currentPlayer == MAX_PLAYERS) {
            currentPlayer = 0;
        }

        // Check if game has ended
        int maxScore = -1;
        int maxPlayer = -1;

        if (currentPlayer == finalPlayer) {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).getScore() > maxScore) {
                    maxPlayer = i;
                    maxScore = players.get(i).getScore();
                }
            }

            if (maxScore >= MAX_SCORE) {
                winner = players.get(maxPlayer).getPlayerId();
            } else {
                finalPlayer = -1;
            }
        }
    }

    public boolean isFinalRound() {
        return finalPlayer != -1;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public boolean ended() {
        return winner != null;
    }

    public PlayerId getWinner() {
        return winner;
    }

    public FortuneCard getCurrentCard() {
        return currentCard;
    }

    public PlayerId getCurrentPlayerId() {
        return players.get(currentPlayer).getPlayerId();
    }

    /**
     * Allows the next fortune card to be rigged.
     *
     * @param fortuneCard next fortune card
     */
    void setRiggedFortuneCard(FortuneCard fortuneCard) {
        currentCard = fortuneCard;
    }
}
