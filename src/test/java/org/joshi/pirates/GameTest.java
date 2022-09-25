package org.joshi.pirates;


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
        game.endTurn(1000);

        player = game.startTurn();
        assertEquals(player, player2.getPlayerId());
        game.endTurn(1000);

        player = game.startTurn();
        assertEquals(player, player3.getPlayerId());
        game.endTurn(1000);

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
        game.endTurn(1000);

        game.startTurn();
        game.endTurn(2000);

        game.startTurn();
        game.endTurn(1500);

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
        game.endTurn(1000);

        game.startTurn();
        game.endTurn(2000);

        game.startTurn();
        game.endTurn(-500);

        var players = game.getPlayers();
        assertEquals(500, players.get(0).getScore());
        assertEquals(1500, players.get(1).getScore());
        assertEquals(0, players.get(2).getScore());

    }
}
