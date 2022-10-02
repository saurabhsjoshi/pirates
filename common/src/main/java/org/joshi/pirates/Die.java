package org.joshi.pirates;

/**
 * Class that represents a single die.
 */
public class Die {
    Side diceSide;
    State state;

    private boolean used = false;

    public Die(Side diceSide) {
        this(diceSide, State.ACTIVE);
    }

    public Die(Side diceSide, State state) {
        this.diceSide = diceSide;
        this.state = state;
    }

    public Side getDiceSide() {
        return diceSide;
    }

    public void setState(State state) {
        this.state = state;
    }

    /**
     * Enum that indicates the current state of a die.
     */
    public enum State {
        HELD,
        ACTIVE,

        IN_TREASURE_CHEST
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isUsed() {
        return used;
    }

    public State getState() {
        return state;
    }

    /**
     * All possible sides of a die.
     */
    public enum Side {
        DIAMOND,
        GOLD_COIN,
        MONKEY,
        PARROT,
        SKULL,
        SWORD
    }
}
