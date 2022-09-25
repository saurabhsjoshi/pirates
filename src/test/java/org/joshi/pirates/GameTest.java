package org.joshi.pirates;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
