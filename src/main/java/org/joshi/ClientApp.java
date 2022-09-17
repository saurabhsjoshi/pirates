package org.joshi;

import org.joshi.network.Client;

import java.io.IOException;

/**
 * Client application that is started by the players who want to connect to the host.
 */
public class ClientApp {

    public static void main(String[] args) throws IOException {
        Client client = new Client(6794, (senderId, msg) -> {
            //TODO: Handle messages
        });

        client.start();
    }
}
