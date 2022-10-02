package org.joshi.pirates.msg;


import org.joshi.network.Message;

import java.util.Map;

/**
 * Message to broadcast all player scores.
 */
public class PlayerScoreMsg extends Message {
    public static final String TYPE = "PlayerScoreMsg";

    private final Map<String, Integer> scores;

    public PlayerScoreMsg(Map<String, Integer> scores) {
        this.scores = scores;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }
}
