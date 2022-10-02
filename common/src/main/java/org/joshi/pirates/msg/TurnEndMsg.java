package org.joshi.pirates.msg;

import org.joshi.network.Message;
import org.joshi.pirates.TurnResult;

public class TurnEndMsg extends Message {
    public static final String TYPE = "TURN_END";

    private final TurnResult result;

    public TurnEndMsg(TurnResult result) {
        this.result = result;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public TurnResult getResult() {
        return result;
    }
}
