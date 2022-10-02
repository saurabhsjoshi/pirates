package org.joshi.pirates.msg;

import org.joshi.network.Message;

public class BroadcastMsg extends Message {
    public static final String TYPE = "BROADCAST";

    private final String message;

    public BroadcastMsg(String message) {
        this.message = message;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public String getMessage() {
        return message;
    }
}
