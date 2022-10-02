package org.joshi.pirates;

import org.joshi.pirates.cards.FortuneCard;
import org.joshi.pirates.cards.SeaBattleCard;
import org.joshi.pirates.cards.SkullCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents a turn of a single player consisting on multiple rolls.
 */
public class Turn {

    public enum State {
        DISQUALIFIED,

        NOT_ENOUGH_ACTIVE_DIE,
        OK
    }

    private boolean isFirstRoll = true;

    private boolean isOnIslandOfSkulls = false;

    private boolean sorceressUsed = false;

    private State state = State.OK;

    private FortuneCard fortuneCard;

    private int heldDiceCount = 0;

    public static class SkullActivatedException extends Exception {
    }

    /**
     * Maximum number of dice that can be played in a turn.
     */
    private static final int MAX_DICE = 8;

    /**
     * Map of the id and dice for this turn.
     */
    private final List<Die> dice = new ArrayList<>(MAX_DICE);

    /**
     * Class that allows rigging of die.
     */
    public record RiggedDie(int index, Die die) {
    }

    /**
     * Mark the die with given index as being held.
     *
     * @param index list of indexes to mark as held
     */
    public void hold(List<Integer> index) {
        for (var i : index) {
            dice.get(i).setState(Die.State.HELD);
        }
    }

    /**
     * Mark the die with given index as active allowing it to be re-rolled.
     *
     * @param index list of index to mark as active
     * @throws SkullActivatedException exception is thrown when the player attempts to activate a skull
     */
    public void active(List<Integer> index) throws SkullActivatedException {
        int skull = 0;

        // Check for skulls
        for (var i : index) {
            if (dice.get(i).getDiceSide() == Die.Side.SKULL) {
                skull++;
            }
        }

        boolean invalidSkulls = false;
        if (fortuneCard != null && fortuneCard.getType() == FortuneCard.Type.SORCERESS && !sorceressUsed) {
            if (skull > 1) {
                invalidSkulls = true;
            } else {
                sorceressUsed = true;
            }
        } else {
            if (skull != 0) {
                invalidSkulls = true;
            }
        }

        if (invalidSkulls) {
            throw new SkullActivatedException();
        }

        for (var i : index) {
            dice.get(i).setState(Die.State.ACTIVE);
        }
    }

    public void roll() {
        var diceSides = Die.Side.values();

        // First roll
        if (dice.isEmpty()) {

            for (int i = 0; i < MAX_DICE; i++) {
                dice.add(new Die(diceSides[(new Random().nextInt(diceSides.length))], Die.State.ACTIVE));
            }
            return;
        }

        for (int i = 0; i < MAX_DICE; i++) {
            if (dice.get(i).state == Die.State.ACTIVE) {
                dice.set(i, new Die(diceSides[(new Random().nextInt(diceSides.length))], Die.State.ACTIVE));
            }
        }
        isFirstRoll = false;
    }

    public void postRoll() {
        // Check skulls
        for (var die : dice) {
            if (die.diceSide == Die.Side.SKULL && die.state != Die.State.HELD) {
                heldDiceCount++;
                die.setState(Die.State.HELD);
            }
        }
        updateState();
    }

    public void updateState() {
        int skulls = 0;
        int active = 0;

        if (fortuneCard instanceof SkullCard skullCard) {
            skulls += skullCard.getSkulls();
        }

        for (var die : dice) {
            if (die.state == Die.State.ACTIVE) active++;
            if (die.diceSide == Die.Side.SKULL) skulls++;
        }

        if (isFirstRoll && skulls > 3 && fortuneCard.getType() != FortuneCard.Type.SEA_BATTLE) {
            isOnIslandOfSkulls = true;
        } else if (skulls > 2 && !isOnIslandOfSkulls) {
            state = State.DISQUALIFIED;
            return;
        }

        if (active < 2) {
            state = State.NOT_ENOUGH_ACTIVE_DIE;
            return;
        }

        state = State.OK;
    }

    /**
     * Method that checks if the player is on Island of Skulls.
     */
    boolean onSkullIsland() {
        return isOnIslandOfSkulls;
    }

    public void setOnIslandOfSkulls(boolean onIslandOfSkulls) {
        isOnIslandOfSkulls = onIslandOfSkulls;
    }

    /**
     * End this turn and return the score. If the score is negative, it indicates that other players have lost those
     * points, like in the case of player being on skull island.
     *
     * @return score earned this round
     */
    public TurnResult complete() {
        var expectedChestSize = dice.size();

        // If player is dead they get no score
        if (state == State.DISQUALIFIED) {
            if (fortuneCard instanceof SeaBattleCard seaBattleCard) {
                var count = dice.stream()
                        .filter(s -> s.diceSide == Die.Side.SWORD)
                        .count();
                if (count < seaBattleCard.getSwords()) {
                    return new TurnResult(false, -seaBattleCard.getBonus());
                }
            }

            if (fortuneCard.getType() != FortuneCard.Type.TREASURE_CHEST) {
                return new TurnResult(false, 0);
            }
            // Only die that are protected will be used for scoring
            dice.removeIf(die -> die.state != Die.State.IN_TREASURE_CHEST);
        }

        Stream<Die> bonusObj = Stream.empty();

        if (fortuneCard.getType() == FortuneCard.Type.GOLD) {
            bonusObj = Stream.of(new Die(Die.Side.GOLD_COIN, Die.State.HELD));
            expectedChestSize++;
        } else if (fortuneCard.getType() == FortuneCard.Type.DIAMOND) {
            bonusObj = Stream.of(new Die(Die.Side.DIAMOND, Die.State.HELD));
            expectedChestSize++;
        }

        List<Die> sides = Stream
                .concat(bonusObj, dice.stream()
                        .filter(s -> s.diceSide != Die.Side.SKULL))
                .collect(Collectors.toList());

        boolean hasSkulls = sides.size() != expectedChestSize;

        if (fortuneCard.getType() == FortuneCard.Type.MONKEY_BUSINESS) {
            sides = sides.stream()
                    .peek(s -> {
                        if (s.getDiceSide() == Die.Side.PARROT) {
                            s.diceSide = Die.Side.MONKEY;
                        }
                    })
                    .collect(Collectors.toList());
        }

        var score = Score.getIdenticalDiceScore(sides);
        score += Score.getBonusDieScore(sides);

        // Sea battle
        if (fortuneCard instanceof SeaBattleCard seaBattleCard) {
            var count = dice.stream()
                    .filter(die -> die.diceSide == Die.Side.SWORD)
                    .count();

            if (count < seaBattleCard.getSwords()) {
                return new TurnResult(false, -seaBattleCard.getBonus());
            }

            // Only in case of two swords we need to mark these dice as used for full chest calculation
            if (count == 2) {
                for (var die : sides) {
                    if (die.getDiceSide() == Die.Side.SWORD) {
                        die.setUsed(true);
                    }
                }
            }

            score += seaBattleCard.getBonus();
        }

        boolean isFullChest = !hasSkulls;

        for (var die : sides) {
            if (!die.isUsed()) {
                isFullChest = false;
                break;
            }
        }

        if (isFullChest) {
            score += 500;
        }

        if (isOnIslandOfSkulls) {
            score = -(heldDiceCount * 100);

            if (fortuneCard instanceof SkullCard skullCard) {
                score -= (skullCard.getSkulls() * 100);
            }
        }

        // Captain card
        if (fortuneCard.getType() == FortuneCard.Type.CAPTAIN) {
            return new TurnResult(isOnIslandOfSkulls, score * 2);
        }

        return new TurnResult(isOnIslandOfSkulls, score);
    }

    public State getState() {
        return state;
    }

    public void setFortuneCard(FortuneCard card) {
        this.fortuneCard = card;
    }

    /**
     * Add die with given indices to the treasure chest.
     *
     * @param dieIndex list of indices
     */
    public void addToChest(List<Integer> dieIndex) {
        for (var i : dieIndex) {
            dice.get(i).state = Die.State.IN_TREASURE_CHEST;
        }
    }

    /**
     * Rig the dice.
     *
     * @param riggedDice list of rigged die
     */
    public void setRiggedDice(List<RiggedDie> riggedDice) {
        for (var roll : riggedDice) {
            dice.set(roll.index, roll.die);
        }
    }

    public boolean isOnIslandOfSkulls() {
        return isOnIslandOfSkulls;
    }

    public List<Die> getDice() {
        return dice;
    }
}
