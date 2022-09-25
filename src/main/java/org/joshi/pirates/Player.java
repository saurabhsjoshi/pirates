package org.joshi.pirates;

/**
 * This class indicates a player in the game.
 */
public class Player {

    /**
     * Unique identifier for this player.
     */
    private final PlayerId playerId;

    /**
     * Current score of the player.
     */
    private int score;

    public Player(PlayerId playerId) {
        this.playerId = playerId;
        this.score = 0;
    }

    public PlayerId getPlayerId() {
        return playerId;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int score) {
        this.score += score;
    }
}
