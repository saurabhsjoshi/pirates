package org.joshi.pirates;

import org.joshi.pirates.cards.FortuneCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class represents a turn of a single player consisting on multiple rolls.
 */
public class Turn {

    private boolean isFirstRoll = true;

    private boolean isOnIslandOfSkulls = false;

    private FortuneCard fortuneCard;

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
        var diceSides = Die.Side.values();

        // First roll
        if (dice.isEmpty()) {

            for (int i = 0; i < MAX_DICE; i++) {
                dice.add(new Die(diceSides[(new Random().nextInt(diceSides.length))], Die.State.ACTIVE));
            }
        }

        for (int i = 0; i < MAX_DICE; i++) {
            if (dice.get(i).state == Die.State.ACTIVE) {
                dice.set(i, new Die(diceSides[(new Random().nextInt(diceSides.length))], Die.State.ACTIVE));
            }
        }
        isFirstRoll = false;
    }

    /**
     * Method that checks if the player can re-roll in this turn.
     */
    ReRollState canRoll(List<Die> dice) {
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
    boolean isOnSkullIsland(List<Die> dice) {
        if (!isFirstRoll) {
            return isOnIslandOfSkulls;
        }

        int skulls = 0;

        for (var die : dice) {
            if (die.diceSide == Die.Side.SKULL) {
                skulls++;
            }
        }

        if (skulls > 3) {
            isOnIslandOfSkulls = true;
            return true;
        }

        return false;
    }

    void setFirstRoll(boolean isFirstRoll) {
        this.isFirstRoll = isFirstRoll;
    }

    public void setOnIslandOfSkulls(boolean onIslandOfSkulls) {
        isOnIslandOfSkulls = onIslandOfSkulls;
    }

    public int complete() {
        if (fortuneCard.getType() == FortuneCard.Type.GOLD) {
            return bonusCard(Die.Side.GOLD_COIN);
        }

        Die.Side[] sides = new Die.Side[MAX_DICE];
        for (int i = 0; i < dice.size(); i++) {
            sides[i] = dice.get(i).diceSide;
        }

        var score = Score.getIdenticalObjectScore(sides);
        score += Score.getBonusObjectScore(sides);

        // Captain card
        if (fortuneCard.getType() == FortuneCard.Type.CAPTAIN) {
            return score * 2;
        }

        return score;
    }

    private int bonusCard(Die.Side bonusObj) {
        Die.Side[] sides = new Die.Side[MAX_DICE + 1];
        sides[0] = bonusObj;
        for (int i = 1; i < dice.size() + 1; i++) {
            sides[i] = dice.get(i - 1).diceSide;
        }

        var score = Score.getIdenticalObjectScore(sides);
        return score + Score.getBonusObjectScore(sides);
    }

    public void setFortuneCard(FortuneCard card) {
        this.fortuneCard = card;
    }
}
