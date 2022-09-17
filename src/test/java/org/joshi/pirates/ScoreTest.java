package org.joshi.pirates;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScoreTest {

    /**
     * Return expected score based on number of provided identical kind of objects.
     *
     * @param kind kind of object
     */
    private static int getExpectedIdenticalScore(int kind) {
        if (kind < 3) {
            return 0;
        }
        return switch (kind) {
            case 3 -> 100;
            case 4 -> 200;
            case 5 -> 500;
            case 6 -> 1000;
            case 7 -> 2000;
            case 8 -> 4000;
            default -> -1;
        };
    }

    /**
     * Method that generates data for test case that validate scoring for identical objects on die roll.
     */
    private static Stream<Arguments> ofKindDieRoll() {
        return IntStream.range(1, 9)
                .boxed()
                .map(kind -> {
                    Die.Side[] dieRoll = new Die.Side[kind];
                    for (int i = 0; i < kind; i++) {
                        dieRoll[i] = Die.Side.PARROT;
                    }
                    return Arguments.of(dieRoll, getExpectedIdenticalScore(kind));
                });
    }

    @ParameterizedTest
    @DisplayName("Validate score for identical objects on die roll")
    @MethodSource("ofKindDieRoll")
    void testGetIdenticalObjectScore(Die.Side[] dieRoll, int expectedScore) {
        int score = Score.getIdenticalObjectScore(dieRoll);
        assertEquals(expectedScore, score);
    }

    @DisplayName("Validate three of a kind when roll includes other objects")
    @Test
    void testGetIdenticalObjectScore_DifferentObjects() {
        Die.Side[] dieRoll = {
                Die.Side.MONKEY,
                Die.Side.PARROT,
                Die.Side.MONKEY,
                Die.Side.MONKEY,
                Die.Side.DIAMOND
        };
        int score = Score.getIdenticalObjectScore(dieRoll);
        assertEquals(getExpectedIdenticalScore(3), score);
    }

    @Test
    @DisplayName("Validate score for multiple identical objects")
    void testMultipleIdenticalObject() {
        Die.Side[] dieRoll = {
                Die.Side.MONKEY,
                Die.Side.PARROT,
                Die.Side.MONKEY,
                Die.Side.MONKEY,
                Die.Side.PARROT,
                Die.Side.PARROT,
                Die.Side.PARROT
        };
        int score = Score.getIdenticalObjectScore(dieRoll);
        assertEquals(getExpectedIdenticalScore(3) + getExpectedIdenticalScore(4), score);
    }

    @Test
    @DisplayName("Validate bonus score for diamond and gold")
    void testGetBonusObjectScore() {
        Die.Side[] dieRoll = {Die.Side.GOLD_COIN, Die.Side.DIAMOND};
        int score = Score.getBonusObjectScore(dieRoll);
        assertEquals(200, score);
    }

    @ParameterizedTest
    @DisplayName("Validate score for bonus objects")
    @EnumSource(Die.Side.class)
    void testGetBonusObjectScore_AllSides(Die.Side diceSide) {
        Die.Side[] dieRoll = {diceSide};
        int score = Score.getBonusObjectScore(dieRoll);

        if (diceSide == Die.Side.DIAMOND || diceSide == Die.Side.GOLD_COIN) {
            assertEquals(100, score);
        } else {
            assertEquals(0, score);
        }
    }
}