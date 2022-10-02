package org.joshi.pirates;

import java.io.Serializable;

/**
 * @param id       Unique identifier for this player.
 * @param username Player name.
 */
public record PlayerId(String id, String username) implements Serializable {

}
