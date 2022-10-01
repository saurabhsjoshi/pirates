package org.joshi;

import org.joshi.network.Client;
import org.joshi.network.MessageHandler;
import org.joshi.pirates.msg.RegisterUsrMsg;
import org.joshi.pirates.ui.ConsoleUtils;

import java.io.IOException;
import java.util.Scanner;

/**
 * Client application that is started by the players who want to connect to the host.
 */
public class ClientApp {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        Client client = new Client(6794);

        MessageHandler handler = (senderId, msg) -> {

        };

        client.setMessageHandler(handler);
        client.start();

        client.sendMsg(new RegisterUsrMsg(ConsoleUtils.userPrompt("Enter username")));
    }
}
