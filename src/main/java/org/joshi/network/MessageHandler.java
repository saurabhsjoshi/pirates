package org.joshi.network;

/**
 * Interface to be implemented by message handlers.
 */
public interface MessageHandler {
    void onMessage(String senderId, Message msg);
}
