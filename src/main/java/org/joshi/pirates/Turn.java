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
     * List containing rigged rolls that this turn will have.
     */
    private final List<List<RiggedReRoll>> riggedRolls = new ArrayList<>();

    /**
     * Class that allows rolls to be rigged for individual die.
     */
    public static class RiggedReRoll {
        int index;
        Die die;

        public RiggedReRoll(int index, Die die) {
            this.index = index;
            this.die = die;
        }
    }

    /**
     * Mark the die with given index as being held.
     *
     * @param index list of indexes to mark as held
     */
    void hold(List<Integer> index) {
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
    void active(List<Integer> index) throws SkullActivatedException {
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

    void roll() {
        var diceSides = Die.Side.values();

        // First roll
        if (dice.isEmpty()) {

            for (int i = 0; i < MAX_DICE; i++) {
                dice.add(new Die(diceSides[(new Random().nextInt(diceSides.length))], Die.State.ACTIVE));
            }
            postRoll();
            return;
        }

        for (int i = 0; i < MAX_DICE; i++) {
            if (dice.get(i).state == Die.State.ACTIVE) {
                dice.set(i, new Die(diceSides[(new Random().nextInt(diceSides.length))], Die.State.ACTIVE));
            }
        }
        isFirstRoll = false;
        postRoll();
    }

    void postRoll() {
        if (!riggedRolls.isEmpty()) {
            var riggedRoll = riggedRolls.remove(0);
            for (var roll : riggedRoll) {
                dice.set(roll.index, roll.die);
            }
        }

        // Check skulls
        for (var die : dice) {
            if (die.diceSide == Die.Side.SKULL) {
                die.setState(Die.State.HELD);
            }
        }
        updateState();
    }

    private void updateState() {
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
    public int complete() {

        // If player is dead they get no score
        if (state == State.DISQUALIFIED) {
            if (fortuneCard.getType() != FortuneCard.Type.TREASURE_CHEST) {
                return 0;
            }
            // Only die that are protected will be used for scoring
            dice.removeIf(die -> die.state != Die.State.IN_TREASURE_CHEST);
        }

        Stream<Die.Side> bonusObj = Stream.empty();

        if (fortuneCard.getType() == FortuneCard.Type.GOLD) {
            bonusObj = Stream.of(Die.Side.GOLD_COIN);
        } else if (fortuneCard.getType() == FortuneCard.Type.DIAMOND) {
            bonusObj = Stream.of(Die.Side.DIAMOND);
        }

        List<Die.Side> sides = Stream
                .concat(bonusObj, dice.stream()
                        .filter(s -> s.diceSide != Die.Side.SKULL)
                        .map(s -> s.diceSide))
                .collect(Collectors.toList());

        if (fortuneCard.getType() == FortuneCard.Type.MONKEY_BUSINESS) {
            sides = sides.stream()
                    .map(s -> {
                        if (s == Die.Side.PARROT) {
                            return Die.Side.MONKEY;
                        }
                        return s;
                    })
                    .collect(Collectors.toList());
        }

        var score = Score.getIdenticalObjectScore(sides);
        score += Score.getBonusObjectScore(sides);

        if (isOnIslandOfSkulls) {
            score = 0;
            for (var die : dice) {
                if (die.diceSide == Die.Side.SKULL) {
                    score -= 100;
                }
            }
        }

        // Captain card
        if (fortuneCard.getType() == FortuneCard.Type.CAPTAIN) {
            return score * 2;
        }

        // Sea battle
        if (fortuneCard instanceof SeaBattleCard seaBattleCard) {
            var count = dice.stream()
                    .filter(die -> die.diceSide == Die.Side.SWORD)
                    .count();

            if (count != seaBattleCard.getSwords()) {
                return 0;
            }

            return score + seaBattleCard.getBonus();
        }

        return score;
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
     * Setup rigged rolls for the whole game.
     *
     * @param riggedRolls list of rolls in sequential order
     */
    public void setRiggedRolls(List<List<Die>> riggedRolls) {
        for (var roll : riggedRolls) {
            List<RiggedReRoll> riggedRoll = new ArrayList<>();
            for (int i = 0; i < roll.size(); i++) {
                riggedRoll.add(new RiggedReRoll(i, roll.get(i)));
            }
            this.riggedRolls.add(riggedRoll);
        }
    }

    public void setRiggedReRolls(List<List<RiggedReRoll>> riggedRolls) {
        this.riggedRolls.addAll(riggedRolls);
    }

    public List<Die> getDice() {
        return dice;
    }
}
