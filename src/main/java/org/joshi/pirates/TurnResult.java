package org.joshi.pirates;

/**
 * Class that represents the result of a turn.
 *
 * @param islandOfDead if the player was on island of dead
 * @param score        the score
 */
public record TurnResult(boolean islandOfDead, int score) {
}
