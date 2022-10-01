package org.joshi.pirates;

import java.io.Serializable;

/**
 * Class that represents the result of a turn.
 *
 * @param islandOfDead if the player was on island of dead
 * @param score        the score
 */
public record TurnResult(boolean islandOfDead, int score) implements Serializable {
}
