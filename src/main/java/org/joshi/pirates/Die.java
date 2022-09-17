package org.joshi.pirates;

/**
 * Class that represents a single die.
 */
public class Die {
    Side diceSide;
    State state;

    public Die(Side diceSide, State state) {
        this.diceSide = diceSide;
        this.state = state;
    }

    public Side getDiceSide() {
        return diceSide;
    }

    public void setDiceSide(Side diceSide) {
        this.diceSide = diceSide;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    /**
     * Enum that indicates the current state of a die.
     */
    public enum State {
        HELD,
        ACTIVE
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
