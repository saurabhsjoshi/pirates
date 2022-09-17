package org.joshi.pirates;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a turn of a single player consisting on multiple rolls.
 */
public class Turn {

    /**
     * Maximum number of dice that can be played in a turn.
     */
    private static final int MAX_DICE = 8;

    /**
     * Enum that indicates the current state of a die.
     */
    public enum DieState {
        HELD,
        ACTIVE
    }

    /**
     * Class that represents a single die.
     */
    public static class Die {
        DiceSide diceSide;
        DieState state;

        public DiceSide getDiceSide() {
            return diceSide;
        }

        public void setDiceSide(DiceSide diceSide) {
            this.diceSide = diceSide;
        }

        public DieState getState() {
            return state;
        }

        public void setState(DieState state) {
            this.state = state;
        }
    }

    /**
     * Map of the id and dice for this turn.
     */
    public List<Die> dice = new ArrayList<>(MAX_DICE);

    void roll() {
        // TODO: Implement
    }
}
