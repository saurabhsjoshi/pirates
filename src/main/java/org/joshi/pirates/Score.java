package org.joshi.pirates;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class that contains utility functions to calculate score for different aspects of a die roll.
 */
public class Score {

    /**
     * Function that calculates the score based on die roll for all identical objects.
     *
     * @param diceSides the sides of the dice that have been rolled
     * @return score achieved
     */
    public static int getIdenticalObjectScore(List<Die.Side> diceSides) {
        return diceSides.stream()
                .collect(Collectors.groupingBy(Function.identity()))
                .values()
                .stream()
                .map(sides -> switch (sides.size()) {
                    case 3 -> 100;
                    case 4 -> 200;
                    case 5 -> 500;
                    case 6 -> 1000;
                    case 7 -> 2000;
                    case 8 -> 4000;
                    default -> 0;
                }).mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Function that computes user score based on given dice. The dice provided in the list will be marked as 'used' if
     * they are used as part of the calculation.
     *
     * @param dice list of die that have been rolled
     * @return computed score
     */
    public static int getIdenticalDiceScore(List<Die> dice) {
        var diceSide = dice.stream()
                .map(die -> die.diceSide)
                .collect(Collectors.groupingBy(Function.identity()));

        int score = 0;

        for (var side : diceSide.entrySet()) {
            var size = side.getValue().size();

            if (size < 3) {
                continue;
            }

            score += switch (side.getValue().size()) {
                case 3 -> 100;
                case 4 -> 200;
                case 5 -> 500;
                case 6 -> 1000;
                case 7 -> 2000;
                case 8 -> 4000;
                default -> 0;
            };
            setUsed(side.getKey(), dice);
        }

        return score;
    }
    public static int getBonusDieScore(List<Die> dice) {
        int score = 0;
        for (var die : dice) {
            if (die.getDiceSide() == Die.Side.DIAMOND || die.getDiceSide() == Die.Side.GOLD_COIN) {
                score += 100;
                die.setUsed(true);
            }
        }
        return score;
    }

    private static void setUsed(Die.Side side, List<Die> dice) {
        for (var die : dice) {
            if (die.getDiceSide() == side) {
                die.setUsed(true);
            }
        }
    }

    public static int getBonusObjectScore(List<Die.Side> diceSides) {
        int score = 0;
        for (var side : diceSides) {
            if (side == Die.Side.DIAMOND || side == Die.Side.GOLD_COIN) {
                score += 100;
            }
        }
        return score;
    }
}
