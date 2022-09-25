package org.joshi.pirates;

/**
 * This class indicates a player in the game.
 */
public class Player {

    /**
     * Unique identifier for this player.
     */
    private final String id;

    /**
     * Player name.
     */
    private final String username;

    /**
     * Current score of the player.
     */
    private int score;

    public Player(String id, String username) {
        this.id = id;
        this.username = username;
        this.score = 0;
    }
}
