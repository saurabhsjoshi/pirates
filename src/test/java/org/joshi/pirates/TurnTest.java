package org.joshi.pirates;

import org.joshi.pirates.cards.FortuneCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for each turn consisting of multiple rolls.
 */
public class TurnTest {

    private Turn turn;

    @BeforeEach
    void setup() {
        turn = new Turn();
        turn.roll();
    }

    @DisplayName("Validate first roll of a turn")
    @Test
    void testFirstRoll() {
        turn.dice.replaceAll(__ -> new Die(Die.Side.MONKEY, Die.State.ACTIVE));
        // Validate that eight die are present in a turn
        assertEquals(8, turn.dice.size());

        // Validate all die are active
        for (var dice : turn.dice) {
            assertEquals(Die.State.ACTIVE, dice.state);
        }
    }

    @DisplayName("Test that validates if the player can re-roll based on number of active die")
    @Test
    void testCanRoll_EnoughActiveDie() {
        for (var die : turn.dice) {
            die.setDiceSide(Die.Side.DIAMOND);
            die.setState(Die.State.HELD);
        }

        assertEquals(Turn.ReRollState.NOT_ENOUGH_ACTIVE_DIE, turn.canRoll(turn.dice));

        turn.dice.get(0).setState(Die.State.ACTIVE);
        turn.dice.get(1).setState(Die.State.ACTIVE);

        assertEquals(Turn.ReRollState.OK, turn.canRoll(turn.dice));
    }

    @DisplayName("Test that validates that player cannot re-roll after accumulating three skulls")
    @Test
    void testCanRoll_ThreeSkulls() {
        Die skull = new Die(Die.Side.SKULL, Die.State.HELD);
        turn.dice.set(0, skull);
        turn.dice.set(1, skull);
        turn.dice.set(2, skull);
        for (int i = 3; i < 8; i++) {
            turn.dice.get(i).setDiceSide(Die.Side.DIAMOND);
            turn.dice.get(i).setState(Die.State.ACTIVE);
        }

        assertEquals(Turn.ReRollState.THREE_SKULLS, turn.canRoll(turn.dice));

        turn.dice.set(3, skull);

        assertEquals(Turn.ReRollState.OK, turn.canRoll(turn.dice));
    }

    @DisplayName("Test that validates that player goes to island of skulls on their first roll of four skulls")
    @Test
    void testSkullIsland() {
        turn.setFirstRoll(true);

        // Set at least four skulls
        Die skull = new Die(Die.Side.SKULL, Die.State.ACTIVE);
        for (int i = 0; i < 4; i++) {
            turn.dice.set(i, skull);
        }

        assertTrue(turn.onSkullIsland(turn.dice));
        // Forcefully set first roll to false
        turn.setFirstRoll(false);

        // Ensure that player remains on island of skulls
        assertTrue(turn.onSkullIsland(turn.dice));

        // Forcefully remove player from island of skulls for second roll
        turn.setOnIslandOfSkulls(false);
        // Even with four skulls, the player should not reach island of skulls
        assertFalse(turn.onSkullIsland(turn.dice));
    }

    @DisplayName("Validate that re roll works as expected")
    @Test
    void testReRoll() {
        // Manually set first five held die
        Die diamond = new Die(Die.Side.DIAMOND, Die.State.HELD);
        for (int i = 0; i < 5; i++) {
            turn.dice.set(i, diamond);
        }

        // Manually set other active die
        Die gold = new Die(Die.Side.GOLD_COIN, Die.State.ACTIVE);
        for (int i = 5; i < 8; i++) {
            turn.dice.set(i, gold);
        }

        turn.roll();

        // Total number of die should not change
        assertEquals(8, turn.dice.size());

        // Ensure held die have not changed
        for (int i = 0; i < 5; i++) {
            assertSame(turn.dice.get(i), diamond);
        }

        for (int i = 5; i < 8; i++) {
            assertNotSame(turn.dice.get(i), gold);
        }
    }

    @DisplayName("Validate end of the turn score calculation with captain card")
    @Test
    void testEndTurn_CaptainCard() {
        turn.dice.replaceAll(__ -> new Die(Die.Side.MONKEY, Die.State.HELD));
        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.CAPTAIN));
        var score = turn.complete();
        assertEquals(8000, score);
    }

    @DisplayName("Validate end of the turn score calculation with gold card")
    @Test
    void testEndTurn_Gold() {
        turn.dice.replaceAll(__ -> new Die(Die.Side.GOLD_COIN, Die.State.HELD));
        turn.dice.set(0, new Die(Die.Side.MONKEY, Die.State.HELD));

        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.GOLD));
        var score = turn.complete();

        // 8 of a kind + bonus for each gold coin
        assertEquals(4800, score);
    }

    @DisplayName("Validate end of the turn score calculation with diamond card")
    @Test
    void testEndTurn_Diamond() {
        turn.dice.replaceAll(__ -> new Die(Die.Side.DIAMOND, Die.State.HELD));
        turn.dice.set(0, new Die(Die.Side.MONKEY, Die.State.HELD));

        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.DIAMOND));
        var score = turn.complete();

        // 8 of a kind + bonus for each diamond coin
        assertEquals(4800, score);
    }

    @DisplayName("Validate end of the turn score calculation with monkey business card")
    @Test
    void testEndTurn_Monkey() {
        turn.dice.replaceAll(__ -> new Die(Die.Side.MONKEY, Die.State.HELD));
        turn.dice.set(0, new Die(Die.Side.PARROT, Die.State.HELD));

        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.MONKEY_BUSINESS));
        var score = turn.complete();

        // 8 of a kind
        assertEquals(4000, score);
    }

    @DisplayName("Validate end of the turn score calculation does not include skulls")
    @Test
    void testEndTurn_IgnoreSkulls() {
        turn.dice.replaceAll(__ -> new Die(Die.Side.SKULL, Die.State.HELD));
        turn.dice.set(0, new Die(Die.Side.PARROT, Die.State.HELD));
        turn.dice.set(1, new Die(Die.Side.PARROT, Die.State.HELD));
        turn.dice.set(2, new Die(Die.Side.PARROT, Die.State.HELD));

        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.MONKEY_BUSINESS));
        var score = turn.complete();

        // skulls should be ignored and only 3 of kind parrots should be used to calculate score
        assertEquals(100, score);
    }

    @DisplayName("Validate end of the turn score calculation for when player is on island of skulls")
    @Test
    void testEndTurn_IslandOfSkulls() {
        turn.dice.replaceAll(__ -> new Die(Die.Side.SKULL, Die.State.HELD));
        turn.dice.set(0, new Die(Die.Side.PARROT, Die.State.HELD));
        turn.setOnIslandOfSkulls(true);

        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.MONKEY_BUSINESS));
        var score = turn.complete();

        // 7 skulls (7 X 100)
        assertEquals(-700, score);
    }

    @DisplayName("Validated that when user marks dice for hold they are set to held state")
    @Test
    void testHold() {
        turn.dice.replaceAll(__ -> new Die(Die.Side.GOLD_COIN, Die.State.ACTIVE));
        turn.hold(List.of(0, 1, 2));

        for (int i = 0; i < 3; i++) {
            assertEquals(turn.dice.get(i).state, Die.State.HELD);
        }

        for (int i = 3; i < turn.dice.size(); i++) {
            assertEquals(turn.dice.get(i).state, Die.State.ACTIVE);
        }
    }

    @DisplayName("Validate that post roll skulls are marked as being held")
    @Test
    void validatePostRollSkullCheck() {
        turn.dice.replaceAll(__ -> new Die(Die.Side.PARROT, Die.State.ACTIVE));
        turn.dice.set(0, new Die(Die.Side.SKULL, Die.State.ACTIVE));
        turn.postRoll();
        assertEquals(turn.dice.get(0).state, Die.State.HELD);
    }

}
