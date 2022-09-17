package org.joshi.pirates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
