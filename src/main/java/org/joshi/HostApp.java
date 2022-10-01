package org.joshi;

import org.joshi.network.MessageHandler;
import org.joshi.network.Server;
import org.joshi.pirates.Game;
import org.joshi.pirates.Player;
import org.joshi.pirates.PlayerId;
import org.joshi.pirates.msg.RegisterUsrMsg;
import org.joshi.pirates.msg.StartTurnMsg;
import org.joshi.pirates.msg.TurnEndMsg;
import org.joshi.pirates.ui.ConsoleUtils;
import org.joshi.pirates.ui.PlayerTurn;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * Server application that is started by the host of the game.
 */
public class HostApp {
    private static final CountDownLatch gameEndLatch = new CountDownLatch(1);

    private Game game;

    private Player host;

    private Server server;

    private int MAX_PLAYERS = 3;

    public static void main(String[] args) throws IOException, InterruptedException {
        HostApp app = new HostApp();
        app.start();
    }

    void start() throws IOException, InterruptedException {
        host = new Player(new PlayerId(UUID.randomUUID().toString(), ConsoleUtils.userPrompt("Enter username")));
        server = new Server(6794);

        // Create a game instance
        game = new Game(MAX_PLAYERS);

        // Add host to the game
        game.addPlayer(host);


        MessageHandler handler = ((senderId, msg) -> {
            switch (msg.getType()) {
                case RegisterUsrMsg.TYPE -> {
                    game.addPlayer(new Player(new PlayerId(senderId, ((RegisterUsrMsg) msg).getUsername())));
                    if (game.canPlay()) {
                        ConsoleUtils.startGameMsg();
                        startTurn(game.startTurn());
                    }
                }

                case TurnEndMsg.TYPE -> {
                    game.endTurn(((TurnEndMsg) msg).getResult());
                    if (game.ended()) {
                        gameEndLatch.countDown();
                    } else {
                        startTurn(game.startTurn());
                    }
                }
            }
        });

        server.setMessageHandler(handler);

        // Wait for two players to join
        server.start(MAX_PLAYERS - 1);

        // Wait for game to end
        gameEndLatch.await();
        server.stop();
    }

    void startTurn(PlayerId playerId) throws IOException {
        if (playerId == host.getPlayerId()) {
            PlayerTurn playerTurn = new PlayerTurn(server, game.getCurrentCard());
            var result = playerTurn.start();
            game.endTurn(result);
            startTurn(game.startTurn());
            return;
        }

        server.sendMsg(playerId.id(), new StartTurnMsg(game.getCurrentCard()));
    }
}
