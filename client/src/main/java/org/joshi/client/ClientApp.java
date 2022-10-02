package org.joshi.client;

import org.joshi.network.Client;
import org.joshi.network.MessageHandler;
import org.joshi.pirates.msg.*;
import org.joshi.pirates.ui.ConsoleUtils;
import org.joshi.pirates.ui.PlayerTurn;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Client application that is started by the players who want to connect to the host.
 */
public class ClientApp {
    private static final CountDownLatch gameEndLatch = new CountDownLatch(1);

    private final boolean rigged;

    private final int port;

    public ClientApp(boolean rigged, int port) {
        this.rigged = rigged;
        this.port = port;
    }

    public void start() throws IOException, InterruptedException {

        if (rigged) {
            ConsoleUtils.printSysMsg("RIGGING ENABLED");
            ConsoleUtils.printSysMsg("USING PORT " + port);
        }

        Client client = new Client(port);

        MessageHandler handler = (senderId, msg) -> {
            switch (msg.getType()) {
                case StartTurnMsg.TYPE -> {
                    PlayerTurn turn = new PlayerTurn(((StartTurnMsg) msg).getFortuneCard(), rigged);
                    var result = turn.start();
                    client.sendMsg(new TurnEndMsg(result));
                }
                case BroadcastMsg.TYPE -> System.out.println(((BroadcastMsg) msg).getMessage());
                case PlayerScoreMsg.TYPE -> ConsoleUtils.printPlayerScores(((PlayerScoreMsg) msg).getScores());
                case WinnerMsg.TYPE -> {
                    client.stop();
                    gameEndLatch.countDown();
                    ConsoleUtils.printWinner(((WinnerMsg) msg).getWinnerName());
                }
            }

        };

        client.setMessageHandler(handler);
        client.start();

        client.sendMsg(new RegisterUsrMsg(ConsoleUtils.userPrompt("Enter username")));
        gameEndLatch.await();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        boolean rigged = false;
        int port = 6794;

        for (int i = 0; i < args.length; i++) {

            if (args[i].equals("RIGGED")) {
                rigged = true;
                continue;
            }

            if (args[i].equals("PORT")) {
                port = Integer.parseInt(args[i + 1]);
                i++;
            }
        }

        ClientApp clientApp = new ClientApp(rigged, port);
        clientApp.start();
    }
}
