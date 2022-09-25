package org.joshi.pirates;

import java.util.ArrayList;

/**
 * This class represents a single game.
 */
public class Game {

    private final ArrayList<Player> players = new ArrayList<>(3);

    /**
     * Method to add player to this game.
     *
     * @param player the player to add
     */
    public void addPlayer(Player player) {
        players.add(player);
    }

    /**
     * Method that indicates if the game can start.
     *
     * @return true if it can start
     */
    public boolean canPlay() {
        return players.size() == 3;
    }
}
