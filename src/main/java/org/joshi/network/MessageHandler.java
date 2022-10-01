package org.joshi.network;

import java.io.IOException;

/**
 * Interface to be implemented by message handlers.
 */
public interface MessageHandler {
    void onMessage(String senderId, Message msg) throws IOException;
}
