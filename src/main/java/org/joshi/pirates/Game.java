package org.joshi.pirates;

import java.util.ArrayList;

/**
 * This class represents a single game.
 */
public class Game {

    private static final int MAX_SCORE = 3000;

    private final ArrayList<Player> players = new ArrayList<>(3);

    int currentPlayer = 0;

    /**
     * Identifier of the player who has crossed winning number of points.
     */
    int finalPlayer = -1;

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
        return players.size() == 3;
    }

    /**
     * Start turn of the player
     *
     * @return player id of the player whose turn to start
     */
    public PlayerId startTurn() {
        if (currentPlayer == 3) {
            currentPlayer = 0;
        }
        return players.get(currentPlayer).getPlayerId();
    }

    public void endTurn(int score) {
        if (score < 0) {
            // Player was on island of skulls, update other player scores
            for (int i = 0; i < 3; i++) {
                if (i != currentPlayer) {
                    players.get(i).addScore(score);
                }
            }
        } else {
            players.get(currentPlayer).addScore(score);
            if (finalPlayer == -1 && players.get(currentPlayer).getScore() >= MAX_SCORE) {
                finalPlayer = currentPlayer;
            }
        }

        currentPlayer++;
    }

    boolean isFinalRound() {
        return finalPlayer != -1;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }
}
