package org.joshi.pirates;


import org.joshi.pirates.cards.FortuneCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest {
    private Player player1;
    private Player player2;
    private Player player3;
    private Game game;


    @BeforeEach
    void setup() {
        player1 = new Player(new PlayerId("player1", "player1"));
        player2 = new Player(new PlayerId("player2", "player2"));
        player3 = new Player(new PlayerId("player3", "player3"));

        game = new Game();
    }


    @DisplayName("Validate that game cannot start until at least three player have joined")
    @Test
    void validateGameStart() {
        game.addPlayer(player1);
        assertFalse(game.canPlay());

        game.addPlayer(player2);
        assertFalse(game.canPlay());

        game.addPlayer(player3);
        assertTrue(game.canPlay());
    }

    @DisplayName("Validate the game correctly selects next player.")
    @Test
    void validateNextTurn() {
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(player3);

        var player = game.startTurn();
        assertEquals(player, player1.getPlayerId());
        game.endTurn(new TurnResult(false, 1000));

        player = game.startTurn();
        assertEquals(player, player2.getPlayerId());
        game.endTurn(new TurnResult(false, 1000));

        player = game.startTurn();
        assertEquals(player, player3.getPlayerId());
        game.endTurn(new TurnResult(false, 1000));

        player = game.startTurn();
        assertEquals(player, player1.getPlayerId());
    }

    @DisplayName("Validate completion of player turn works as expected")
    @Test
    void validateCompleteTurn() {
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(player3);

        game.startTurn();
        game.endTurn(new TurnResult(false, 1000));

        game.startTurn();
        game.endTurn(new TurnResult(false, 2000));

        game.startTurn();
        game.endTurn(new TurnResult(false, 1500));

        var players = game.getPlayers();

        assertEquals(1000, players.get(0).getScore());
        assertEquals(2000, players.get(1).getScore());
        assertEquals(1500, players.get(2).getScore());
    }


    @DisplayName("Validate completion of player turn works as expected when they are are on island of skulls")
    @Test
    void validateCompleteTurn_IslandOfSkulls() {
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(player3);

        game.startTurn();
        game.endTurn(new TurnResult(false, 1000));

        game.startTurn();
        game.endTurn(new TurnResult(false, 2000));

        game.startTurn();
        game.endTurn(new TurnResult(true, -500));

        var players = game.getPlayers();
        assertEquals(500, players.get(0).getScore());
        assertEquals(1500, players.get(1).getScore());
        assertEquals(0, players.get(2).getScore());
    }

    @DisplayName("Validate player score cannot be negative")
    @Test
    void validateCompleteTurn_NoNegative() {
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(player3);

        game.startTurn();
        game.endTurn(new TurnResult(false, 1000));

        game.startTurn();
        game.endTurn(new TurnResult(false, 300));

        game.startTurn();
        game.endTurn(new TurnResult(true, -500));

        var players = game.getPlayers();

        assertEquals(500, players.get(0).getScore());
        assertEquals(0, players.get(1).getScore());
        assertEquals(0, players.get(2).getScore());
    }

    @DisplayName("Validate if the final round is identified correctly.")
    @Test
    void validateFinalRound() {
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(player3);

        game.startTurn();
        game.endTurn(new TurnResult(false, 1000));

        game.startTurn();
        game.endTurn(new TurnResult(false, 3000));

        assertTrue(game.isFinalRound());
    }

    @DisplayName("Validate game ends as expected after final round")
    @Test
    void validateGameEnd() {
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(player3);

        game.startTurn();
        game.endTurn(new TurnResult(false, 1000));
        assertFalse(game.ended());

        game.startTurn();
        game.endTurn(new TurnResult(false, 3000));

        assertFalse(game.ended());
        assertTrue(game.isFinalRound());

        game.startTurn();
        game.endTurn(new TurnResult(false, 2000));
        assertFalse(game.ended());

        game.startTurn();
        game.endTurn(new TurnResult(false, 500));

        assertTrue(game.ended());
    }


    @DisplayName("Validate game does not end if player reduces other player scores")
    @Test
    void validateGameEnd_IslandOfSkulls() {
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(player3);

        game.startTurn();
        game.endTurn(new TurnResult(false, 1000));
        assertFalse(game.ended());

        game.startTurn();
        game.endTurn(new TurnResult(false, 3000));

        assertFalse(game.ended());
        assertTrue(game.isFinalRound());

        game.startTurn();
        game.endTurn(new TurnResult(true, -500));

        assertFalse(game.ended());

        game.startTurn();
        game.endTurn(new TurnResult(false, 500));

        assertFalse(game.isFinalRound());
        assertFalse(game.ended());
    }

    @DisplayName("Validate a card is drawn after turn starts")
    @Test
    void validateCardDraw() {
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(player3);

        game.startTurn();
        assertNotNull(game.getCurrentCard());
    }

    @DisplayName("Validate that the card deck rolls over if it runs out of cards")
    @Test
    void validateCardDraw_Rollover() {
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(player3);

        for (int i = 0; i < 36; i++) {
            game.startTurn();
        }
        assertNotNull(game.getCurrentCard());
    }

    @DisplayName("Validate rigging of fortune card works as expected")
    @Test
    void validateRiggedFortuneCard() {
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(player3);

        FortuneCard card = new FortuneCard(FortuneCard.Type.DIAMOND);

        game.startTurn();
        game.setRiggedFortuneCard(card);

        // Validate same instance
        assertSame(card, game.getCurrentCard());
    }
}
