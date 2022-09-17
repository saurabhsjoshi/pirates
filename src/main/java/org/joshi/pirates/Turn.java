package org.joshi.pirates;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class represents a turn of a single player consisting on multiple rolls.
 */
public class Turn {

    /**
     * Enum to indicate if the player can re-roll.
     */
    enum ReRollState {
        NOT_ENOUGH_ACTIVE_DIE,
        THREE_SKULLS,
        OK
    }

    /**
     * Maximum number of dice that can be played in a turn.
     */
    private static final int MAX_DICE = 8;

    /**
     * Map of the id and dice for this turn.
     */
    public List<Die> dice = new ArrayList<>(MAX_DICE);

    void roll() {
        // First roll
        if (dice.isEmpty()) {
            var diceSides = Die.Side.values();
            for (int i = 0; i < MAX_DICE; i++) {
                dice.add(new Die(diceSides[(new Random().nextInt(diceSides.length))], Die.State.ACTIVE));
            }
        }
    }

    ReRollState canRoll() {
        // TODO: Implement
        return ReRollState.OK;
    }
}
