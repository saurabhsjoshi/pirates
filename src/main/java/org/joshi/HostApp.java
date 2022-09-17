package org.joshi;

import org.joshi.network.Server;

import java.io.IOException;

/**
 * Server application that is started by the host of the game.
 */
public class HostApp {

    public static void main(String[] args) throws IOException {
        System.out.println("Starting server");
        Server server = new Server(6794, (senderId, msg) -> {
            // TODO: Handle messages from clients
        });

        server.start(2);
    }
}
