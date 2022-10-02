package org.joshi.pirates;

import java.io.Serializable;

/**
 * This class indicates a player in the game.
 */
public class Player implements Serializable {

    /**
     * Unique identifier for this player.
     */
    private final PlayerId playerId;

    /**
     * Current score of the player.
     */
    private Integer score = 0;

    public Player(PlayerId playerId) {
        this.playerId = playerId;
    }

    public PlayerId getPlayerId() {
        return playerId;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int score) {
        this.score += score;

        // Score can never be negative.
        if (this.score < 0) {
            this.score = 0;
        }
    }
}
