package org.joshi;

import org.joshi.network.MessageHandler;
import org.joshi.network.Server;

import java.io.IOException;

/**
 * Server application that is started by the host of the game.
 */
public class HostApp {

    public static void main(String[] args) throws IOException {
        final Server server = new Server(6794);
        MessageHandler handler = ((senderId, msg) -> {

        });

        server.setMessageHandler(handler);
        server.start(2);
    }
}
