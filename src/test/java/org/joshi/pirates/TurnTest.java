package org.joshi.pirates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        assertEquals(Turn.ReRollState.NOT_ENOUGH_ACTIVE_DIE, turn.canRoll());

        turn.dice.get(0).setState(Die.State.ACTIVE);
        turn.dice.get(1).setState(Die.State.ACTIVE);

        assertEquals(Turn.ReRollState.OK, turn.canRoll());
    }

    @DisplayName("Test that validates that player cannot re-roll after accumulating three skulls")
    @Test
    void testCanRoll_ThreeSkulls() {
        Die skull = new Die(Die.Side.SKULL, Die.State.HELD);
        turn.dice.add(0, skull);
        turn.dice.add(1, skull);
        turn.dice.add(2, skull);
        for (int i = 3; i < 8; i++) {
            turn.dice.get(i).setDiceSide(Die.Side.DIAMOND);
        }

        assertEquals(Turn.ReRollState.THREE_SKULLS, turn.canRoll());

        turn.dice.add(3, skull);

        assertEquals(Turn.ReRollState.OK, turn.canRoll());
    }
}
