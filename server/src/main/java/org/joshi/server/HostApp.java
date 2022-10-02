package org.joshi.server;

import org.joshi.network.MessageHandler;
import org.joshi.network.Server;
import org.joshi.pirates.Game;
import org.joshi.pirates.Player;
import org.joshi.pirates.PlayerId;
import org.joshi.pirates.TurnResult;
import org.joshi.pirates.msg.*;
import org.joshi.pirates.ui.ConsoleUtils;
import org.joshi.pirates.ui.PlayerTurn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * Server application that is started by the host of the game.
 */
public class HostApp {
    private static final CountDownLatch gameEndLatch = new CountDownLatch(1);

    private final Game game;

    private Player host;

    private final int port;

    private Server server;

    private final boolean riggingEnabled;

    private final int MAX_PLAYERS;

    public static void main(String[] args) throws IOException, InterruptedException {
        int players = 3;
        boolean rigged = false;
        int port = 6794;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("PLAYERS")) {
                players = Integer.parseInt(args[i + 1]);
                i++;
                continue;
            }

            if (args[i].equals("RIGGED")) {
                rigged = true;
                continue;
            }

            if (args[i].equals("PORT")) {
                port = Integer.parseInt(args[i + 1]);
                i++;
            }
        }
        HostApp app = new HostApp(rigged, players, port);
        app.start();
    }

    public HostApp(boolean riggingEnabled, int maxPlayers, int port) {
        this.riggingEnabled = riggingEnabled;
        this.port = port;
        MAX_PLAYERS = maxPlayers;

        // Create a game instance
        game = new Game(MAX_PLAYERS);
    }

    void start() throws IOException, InterruptedException {
        if (riggingEnabled) {
            ConsoleUtils.printSysMsg("RIGGING ENABLED");
        }

        host = new Player(new PlayerId(UUID.randomUUID().toString(), ConsoleUtils.userPrompt("Enter username to start server")));
        server = new Server(port);

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

                case TurnEndMsg.TYPE -> postTurn(((TurnEndMsg) msg).getResult());
            }
        });

        server.setMessageHandler(handler);

        // Single player
        if (MAX_PLAYERS == 1) {
            ConsoleUtils.startGameMsg();
            startTurn(game.startTurn());
        }

        // Wait for two players to join
        server.start(MAX_PLAYERS - 1);

        // Wait for game to end
        gameEndLatch.await();
    }

    void postTurn(TurnResult result) throws IOException {

        server.broadcast(new BroadcastMsg(ConsoleUtils.getEndTurnMsg(game.getCurrentPlayerId().username())));
        System.out.println(ConsoleUtils.getEndTurnMsg(game.getCurrentPlayerId().username()));

        game.endTurn(result);

        Map<String, Integer> scores = new HashMap<>();
        for (var player : game.getPlayers()) {
            scores.put(player.getPlayerId().username(), player.getScore());
        }
        ConsoleUtils.printPlayerScores(game.getPlayers());
        server.broadcast(new PlayerScoreMsg(scores));

        if (game.ended()) {
            ConsoleUtils.printWinner(game.getWinner().username());
            server.broadcast(new WinnerMsg(game.getWinner().username()));
            gameEndLatch.countDown();
            server.stop();
            return;
        }

        if (game.isFinalRound()) {
            server.broadcast(new BroadcastMsg(ConsoleUtils.getSysMsg("FINAL ROUND")));
            ConsoleUtils.printSysMsg("FINAL ROUND");
        }

        startTurn(game.startTurn());
    }

    void startTurn(PlayerId playerId) throws IOException {
        server.broadcast(new BroadcastMsg(ConsoleUtils.getStartTurnMsg(playerId.username())));
        System.out.println(ConsoleUtils.getStartTurnMsg(playerId.username()));

        if (playerId == host.getPlayerId()) {
            PlayerTurn playerTurn = new PlayerTurn(game.getCurrentCard(), riggingEnabled);
            postTurn(playerTurn.start());
            return;
        }

        server.sendMsg(playerId.id(), new StartTurnMsg(game.getCurrentCard()));
    }
}
