package org.joshi.pirates;

import org.joshi.pirates.cards.FortuneCard;
import org.joshi.pirates.cards.SeaBattleCard;
import org.joshi.pirates.cards.SkullCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    }

    @DisplayName("Validate first roll of a turn")
    @Test
    void testFirstRoll() {
        List<List<Die>> riggedRolls = new ArrayList<>();
        riggedRolls.add(new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.MONKEY, Die.State.ACTIVE))));
        turn.setRiggedRolls(riggedRolls);

        turn.roll();

        var dice = turn.getDice();

        // Validate that eight die are present in a turn
        assertEquals(8, dice.size());

        // Validate all die are active
        for (var die : dice) {
            assertEquals(Die.State.ACTIVE, die.state);
        }
    }

    @DisplayName("Test that validates if the player can re-roll based on number of active die")
    @Test
    void testCanRoll_EnoughActiveDie() {
        List<List<Die>> riggedRolls = new ArrayList<>();

        riggedRolls.add(new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.DIAMOND, Die.State.HELD))));
        riggedRolls.add(new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.DIAMOND, Die.State.HELD))));
        riggedRolls.get(1).get(0).setState(Die.State.ACTIVE);
        riggedRolls.get(1).get(1).setState(Die.State.ACTIVE);
        turn.setRiggedRolls(riggedRolls);

        turn.roll();

        assertEquals(turn.getState(), Turn.State.NOT_ENOUGH_ACTIVE_DIE);

        turn.roll();

        assertEquals(turn.getState(), Turn.State.OK);
    }

    @DisplayName("Test that validates that player cannot re-roll after accumulating three skulls")
    @Test
    void testCanRoll_ThreeSkulls() {
        List<List<Die>> riggedRolls = new ArrayList<>();
        List<Die> roll = new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.DIAMOND, Die.State.HELD)));
        Die skull = new Die(Die.Side.SKULL, Die.State.HELD);
        roll.set(0, skull);
        roll.set(1, skull);
        roll.set(2, skull);
        riggedRolls.add(roll);
        turn.setRiggedRolls(riggedRolls);

        turn.roll();

        assertEquals(turn.getState(), Turn.State.DISQUALIFIED);
    }

    @DisplayName("Test that validates that player cannot re-roll after accumulating three skulls via skulls card")
    @Test
    void testCanRoll_ThreeSkulls_SkullsCard() {
        List<List<Die>> riggedRolls = new ArrayList<>();
        List<Die> roll = new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.DIAMOND, Die.State.HELD)));
        Die skull = new Die(Die.Side.SKULL, Die.State.HELD);
        roll.set(0, skull);
        roll.set(1, skull);
        riggedRolls.add(roll);

        turn.setRiggedRolls(riggedRolls);
        turn.setFortuneCard(new SkullCard(1));

        turn.roll();

        assertEquals(turn.getState(), Turn.State.DISQUALIFIED);
    }


    @DisplayName("Test that validates that player goes to island of skulls on their first roll of four skulls")
    @Test
    void testSkullIsland() {
        List<List<Die>> riggedRolls = new ArrayList<>();
        List<Die> roll = new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.DIAMOND, Die.State.HELD)));
        // Set at least four skulls
        Die skull = new Die(Die.Side.SKULL, Die.State.ACTIVE);
        for (int i = 0; i < 4; i++) {
            roll.set(i, skull);
        }
        riggedRolls.add(roll);
        riggedRolls.add(roll);
        riggedRolls.add(roll);
        turn.setRiggedRolls(riggedRolls);
        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.GOLD));

        turn.roll();

        assertTrue(turn.onSkullIsland());

        // Ensure that player remains on island of skulls
        turn.roll();
        assertTrue(turn.onSkullIsland());

        // Forcefully remove player from island of skulls for second roll
        turn.setOnIslandOfSkulls(false);

        turn.roll();
        // Even with four skulls, the player should not reach island of skulls
        assertFalse(turn.onSkullIsland());
    }

    @DisplayName("Test that validates that player goes to island of skulls on their first roll with skulls card")
    @Test
    void testSkullIsland_SkullsCard() {
        List<List<Die>> riggedRolls = new ArrayList<>();
        List<Die> roll = new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.MONKEY, Die.State.HELD)));
        // Set at least four skulls
        Die skull = new Die(Die.Side.SKULL, Die.State.ACTIVE);
        for (int i = 0; i < 2; i++) {
            roll.set(i, skull);
        }
        riggedRolls.add(roll);

        turn.setRiggedRolls(riggedRolls);
        turn.setFortuneCard(new SkullCard(2));

        turn.roll();

        assertTrue(turn.onSkullIsland());
    }

    @DisplayName("Validate that re roll works as expected")
    @Test
    void testReRoll() {
        List<Die> roll = new ArrayList<>();

        // Manually set first five held die
        Die diamond = new Die(Die.Side.DIAMOND, Die.State.HELD);
        for (int i = 0; i < 5; i++) {
            roll.add(diamond);
        }

        // Manually set other active die
        Die gold = new Die(Die.Side.GOLD_COIN, Die.State.ACTIVE);
        for (int i = 0; i < 3; i++) {
            roll.add(gold);
        }

        List<List<Die>> riggedRolls = new ArrayList<>(List.of(roll));
        turn.setRiggedRolls(riggedRolls);

        turn.roll();
        turn.roll();

        var dice = turn.getDice();

        // Total number of die should not change
        assertEquals(8, dice.size());


        // Ensure held die have not changed
        for (int i = 0; i < 5; i++) {
            assertSame(dice.get(i), diamond);
        }

        for (int i = 5; i < 8; i++) {
            assertNotSame(dice.get(i), gold);
        }
    }

    @DisplayName("Validate end of the turn score calculation with captain card")
    @Test
    void testEndTurn_CaptainCard() {
        List<List<Die>> riggedRolls = new ArrayList<>(List.of(Collections.nCopies(8, new Die(Die.Side.MONKEY, Die.State.ACTIVE))));
        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.CAPTAIN));
        turn.setRiggedRolls(riggedRolls);
        turn.roll();

        var score = turn.complete();
        assertEquals(8000, score);
    }

    @DisplayName("Validate end of the turn score calculation with gold card")
    @Test
    void testEndTurn_Gold() {
        List<List<Die>> riggedRolls = new ArrayList<>(List.of(new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.GOLD_COIN, Die.State.HELD)))));
        riggedRolls.get(0).set(0, new Die(Die.Side.MONKEY, Die.State.HELD));

        turn.setRiggedRolls(riggedRolls);
        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.GOLD));

        turn.roll();

        var score = turn.complete();

        // 8 of a kind + bonus for each gold coin
        assertEquals(4800, score);
    }

    @DisplayName("Validate end of the turn score calculation with diamond card")
    @Test
    void testEndTurn_Diamond() {
        List<List<Die>> riggedRolls = new ArrayList<>(List.of(new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.DIAMOND, Die.State.HELD)))));
        riggedRolls.get(0).set(0, new Die(Die.Side.MONKEY, Die.State.HELD));

        turn.setRiggedRolls(riggedRolls);
        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.DIAMOND));

        turn.roll();

        var score = turn.complete();

        // 8 of a kind + bonus for each diamond coin
        assertEquals(4800, score);
    }

    @DisplayName("Validate end of the turn score calculation with monkey business card")
    @Test
    void testEndTurn_Monkey() {
        List<List<Die>> riggedRolls = new ArrayList<>(List.of(new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.MONKEY, Die.State.HELD)))));
        riggedRolls.get(0).set(0, new Die(Die.Side.PARROT, Die.State.HELD));

        turn.setRiggedRolls(riggedRolls);
        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.MONKEY_BUSINESS));

        turn.roll();

        var score = turn.complete();

        // 8 of a kind
        assertEquals(4000, score);
    }

    @DisplayName("Validate end of the turn score calculation for when player is on island of skulls")
    @Test
    void testEndTurn_IslandOfSkulls() {
        List<List<Die>> riggedRolls = new ArrayList<>(List.of(new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.SKULL, Die.State.HELD)))));
        riggedRolls.get(0).set(0, new Die(Die.Side.PARROT, Die.State.HELD));

        turn.setRiggedRolls(riggedRolls);
        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.MONKEY_BUSINESS));

        turn.roll();

        var score = turn.complete();

        // 7 skulls (7 X 100)
        assertEquals(-700, score);
    }

    @DisplayName("Validated that when user marks dice for hold they are set to held state")
    @Test
    void testHold() {
        List<Die> roll = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            roll.add(new Die(Die.Side.GOLD_COIN, Die.State.ACTIVE));
        }

        List<List<Die>> riggedRolls = new ArrayList<>(List.of(roll));
        turn.setRiggedRolls(riggedRolls);

        turn.roll();

        turn.hold(List.of(0, 1, 2));

        var dice = turn.getDice();

        for (int i = 0; i < 3; i++) {
            assertEquals(dice.get(i).state, Die.State.HELD);
        }

        for (int i = 3; i < dice.size(); i++) {
            assertEquals(dice.get(i).state, Die.State.ACTIVE);
        }
    }

    @DisplayName("Validated that when user marks dice for active they are set to active state unless they are skulls")
    @Test
    void testActive_Skulls() {
        List<List<Die>> riggedRolls = new ArrayList<>(List.of(new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.GOLD_COIN, Die.State.HELD)))));
        riggedRolls.get(0).set(0, new Die(Die.Side.SKULL, Die.State.HELD));
        turn.setRiggedRolls(riggedRolls);

        turn.roll();

        assertThrows(Turn.SkullActivatedException.class, () -> turn.active(List.of(0, 1, 2)));
        assertDoesNotThrow(() -> turn.active(List.of(1, 2)));

        for (int i = 1; i < 3; i++) {
            assertEquals(turn.getDice().get(i).state, Die.State.ACTIVE);
        }
    }

    @DisplayName("Validate activation of one skull using sorceress card.")
    @Test
    void testActive_Sorceress() {
        List<List<Die>> riggedRolls = new ArrayList<>(List.of(new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.GOLD_COIN, Die.State.HELD)))));
        riggedRolls.get(0).set(0, new Die(Die.Side.SKULL, Die.State.HELD));
        riggedRolls.get(0).set(1, new Die(Die.Side.SKULL, Die.State.HELD));

        turn.setRiggedRolls(riggedRolls);
        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.SORCERESS));

        turn.roll();

        // Cannot activate more than one skull
        assertThrows(Turn.SkullActivatedException.class, () -> turn.active(List.of(0, 1)));

        // Allow activation one skull
        assertDoesNotThrow(() -> turn.active(List.of(0)));

        // Should not be allowed to activate it again
        assertThrows(Turn.SkullActivatedException.class, () -> turn.active(List.of(1)));
    }

    @DisplayName("Validate that post roll skulls are marked as being held")
    @Test
    void validatePostRollSkullCheck() {
        List<List<Die>> riggedRolls = new ArrayList<>(List.of(new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.PARROT, Die.State.ACTIVE)))));
        riggedRolls.get(0).set(0, new Die(Die.Side.SKULL, Die.State.ACTIVE));
        turn.setRiggedRolls(riggedRolls);

        turn.roll();

        assertEquals(turn.getDice().get(0).state, Die.State.HELD);
    }


    @DisplayName("Validate that rolls can be rigged")
    @Test
    void testRollRigging() {
        List<List<Die>> riggedRolls = new ArrayList<>();

        // Insert rigged rolls
        riggedRolls.add(new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.PARROT, Die.State.HELD))));
        riggedRolls.add(new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.DIAMOND, Die.State.HELD))));
        riggedRolls.add(new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.GOLD_COIN, Die.State.HELD))));

        // Setup new turn
        turn = new Turn();
        turn.setRiggedRolls(riggedRolls);

        turn.roll();
        assertEquals(turn.getDice(), riggedRolls.get(0));

        turn.roll();
        assertEquals(turn.getDice(), riggedRolls.get(1));

        turn.roll();
        assertEquals(turn.getDice(), riggedRolls.get(2));

    }

    @DisplayName("Validate player cannot go to Island of the Dead when in Sea Battle")
    @Test
    void testSeaBattle_IslandOfDead() {
        List<List<Die>> riggedRolls = new ArrayList<>(List.of(new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.SKULL, Die.State.ACTIVE)))));
        turn.setRiggedRolls(riggedRolls);
        turn.setFortuneCard(new SeaBattleCard(2, 300));

        turn.roll();

        assertFalse(turn.onSkullIsland());
    }

    @DisplayName("Validate bonus is received with Sea Battle")
    @Test
    void testSeaBattle_Bonus() {
        List<List<Die>> riggedRolls = new ArrayList<>(List.of(new ArrayList<>(Collections.nCopies(8, new Die(Die.Side.PARROT, Die.State.ACTIVE)))));
        riggedRolls.get(0).set(0, new Die(Die.Side.SWORD, Die.State.ACTIVE));
        riggedRolls.get(0).set(1, new Die(Die.Side.SWORD, Die.State.ACTIVE));
        turn.setRiggedRolls(riggedRolls);
        turn.setFortuneCard(new SeaBattleCard(2, 300));
        turn.roll();

        var score = turn.complete();

        // 6 of a kind 1000 + sea battle bonus of 300
        assertEquals(1300, score);
    }

}
