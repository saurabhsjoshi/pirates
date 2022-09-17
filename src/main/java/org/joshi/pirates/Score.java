package org.joshi.pirates;

import java.util.Arrays;
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
    public static int getIdenticalObjectScore(DiceSide[] diceSides) {
        return Arrays.stream(diceSides)
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

    public static int getBonusObjectScore(DiceSide[] diceSides) {
        int score = 0;
        for (var side : diceSides) {
            if (side == DiceSide.DIAMOND || side == DiceSide.GOLD_COIN) {
                score += 100;
            }
        }
        return score;
    }
}
