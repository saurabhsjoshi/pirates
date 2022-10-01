package org.joshi.pirates.msg;

import org.joshi.network.Message;

/**
 * Message that is sent when a new user joins the game.
 */
public class RegisterUsrMsg extends Message {
    public static final String TYPE = "REGISTER_USER";

    private final String username;

    public RegisterUsrMsg(String username) {
        this.username = username;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public String getUsername() {
        return username;
    }
}
