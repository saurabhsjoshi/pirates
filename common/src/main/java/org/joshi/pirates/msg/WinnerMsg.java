package org.joshi.pirates.msg;

import org.joshi.network.Message;

public class WinnerMsg extends Message {
    public static final String TYPE = "WINNER_MSG";

    private final String winnerName;

    public WinnerMsg(String winnerName) {
        this.winnerName = winnerName;
    }

    public String getWinnerName() {
        return winnerName;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
