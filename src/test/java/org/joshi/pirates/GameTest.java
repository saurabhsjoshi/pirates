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
        Player player1 = new Player(new PlayerId("player1", "player1"));
        Player player2 = new Player(new PlayerId("player2", "player2"));
        Player player3 = new Player(new PlayerId("player3", "player3"));

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

        player = game.startTurn();
        assertEquals(player, player2.getPlayerId());

        player = game.startTurn();
        assertEquals(player, player3.getPlayerId());

        player = game.startTurn();
        assertEquals(player, player1.getPlayerId());
    }
}
