package org.joshi.pirates.msg;


import org.joshi.network.Message;
import org.joshi.pirates.Player;

import java.util.ArrayList;

/**
 * Message to broadcast all player scores.
 */
public class PlayerScoreMsg extends Message {
    public static final String TYPE = "PlayerScoreMsg";

    private final ArrayList<Player> players;

    public PlayerScoreMsg(ArrayList<Player> players) {
        this.players = players;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }
}
