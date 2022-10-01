package org.joshi;

import org.joshi.network.MessageHandler;
import org.joshi.network.Server;
import org.joshi.pirates.Game;
import org.joshi.pirates.Player;
import org.joshi.pirates.PlayerId;
import org.joshi.pirates.msg.RegisterUsrMsg;
import org.joshi.pirates.ui.ConsoleUtils;

import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * Server application that is started by the host of the game.
 */
public class HostApp {
    private static final CountDownLatch gameStartLatch = new CountDownLatch(1);
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException, InterruptedException {
        final Server server = new Server(6794);


        Player host = new Player(new PlayerId(UUID.randomUUID().toString(), ConsoleUtils.userPrompt("Enter username")));

        // Create a game instance
        Game game = new Game();

        // Add host to the game
        game.addPlayer(host);


        MessageHandler handler = ((senderId, msg) -> {
            switch (msg.getType()) {
                case RegisterUsrMsg.TYPE:
                    game.addPlayer(new Player(new PlayerId(senderId, ((RegisterUsrMsg) msg).getUsername())));
                    if (game.canPlay()) {
                        gameStartLatch.countDown();
                    }
                    break;
            }
        });

        server.setMessageHandler(handler);

        // Wait for two players to join
        server.start(2);
        gameStartLatch.await();

        ConsoleUtils.startGameMsg();

        while (!game.ended()) {
            //TODO: Implement
        }
    }
}
