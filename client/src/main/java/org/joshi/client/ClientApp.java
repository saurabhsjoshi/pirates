package org.joshi.client;

import org.joshi.network.Client;
import org.joshi.network.MessageHandler;
import org.joshi.pirates.msg.PlayerScoreMsg;
import org.joshi.pirates.msg.RegisterUsrMsg;
import org.joshi.pirates.msg.StartTurnMsg;
import org.joshi.pirates.msg.TurnEndMsg;
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

    public ClientApp(boolean rigged) {
        this.rigged = rigged;
    }

    public void start() throws IOException, InterruptedException {

        if (rigged) {
            ConsoleUtils.printSysMsg("RIGGING ENABLED");
        }

        Client client = new Client(6794);

        MessageHandler handler = (senderId, msg) -> {
            switch (msg.getType()) {
                case StartTurnMsg.TYPE -> {
                    PlayerTurn turn = new PlayerTurn(((StartTurnMsg) msg).getFortuneCard(), rigged);
                    var result = turn.start();
                    client.sendMsg(new TurnEndMsg(result));
                }

                case PlayerScoreMsg.TYPE -> ConsoleUtils.printPlayerScores(((PlayerScoreMsg) msg).getPlayers());
            }

        };

        client.setMessageHandler(handler);
        client.start();

        client.sendMsg(new RegisterUsrMsg(ConsoleUtils.userPrompt("Enter username")));
        gameEndLatch.await();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        boolean rigged = false;
        for (var arg : args) {
            if (arg.equals("RIGGED")) {
                rigged = true;
                break;
            }
        }

        ClientApp clientApp = new ClientApp(rigged);
        clientApp.start();
    }


}
