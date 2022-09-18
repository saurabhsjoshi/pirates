package org.joshi.pirates;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class represents a turn of a single player consisting on multiple rolls.
 */
public class Turn {

    private boolean isFirstRoll;

    private boolean isOnIslandOfSkulls;

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

    /**
     * Method that checks if the player can re-roll in this turn.
     */
    ReRollState canRoll() {
        int skulls = 0;
        int active = 0;

        for (var die : dice) {
            if (die.state == Die.State.ACTIVE)
                active++;
            if (die.diceSide == Die.Side.SKULL)
                skulls++;
        }

        if (active < 2) {
            return ReRollState.NOT_ENOUGH_ACTIVE_DIE;
        }

        if (skulls == 3) {
            return ReRollState.THREE_SKULLS;
        }

        return ReRollState.OK;
    }

    /**
     * Method that checks if the player is on Island of Skulls.
     */
    boolean isOnSkullIsland() {
        // TODO: Implement
        return false;
    }

    void setFirstRoll(boolean isFirstRoll) {
        // TODO: Implement
    }

    public void setOnIslandOfSkulls(boolean onIslandOfSkulls) {
        // TODO: Implement
    }
}
