package org.joshi;

import org.joshi.network.MessageHandler;
import org.joshi.network.Server;
import org.joshi.pirates.Game;
import org.joshi.pirates.Player;
import org.joshi.pirates.PlayerId;
import org.joshi.pirates.TurnResult;
import org.joshi.pirates.msg.PlayerScoreMsg;
import org.joshi.pirates.msg.RegisterUsrMsg;
import org.joshi.pirates.msg.StartTurnMsg;
import org.joshi.pirates.msg.TurnEndMsg;
import org.joshi.pirates.ui.ConsoleUtils;
import org.joshi.pirates.ui.PlayerTurn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * Server application that is started by the host of the game.
 */
public class HostApp {
    private static final CountDownLatch gameEndLatch = new CountDownLatch(1);

    private final Game game;

    private Player host;

    private Server server;

    private final boolean riggingEnabled;

    private final int MAX_PLAYERS;

    public static void main(String[] args) throws IOException, InterruptedException {
        int players = 3;
        boolean rigged = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("PLAYERS")) {
                players = Integer.parseInt(args[i + i]);
                i++;
                continue;
            }

            if (args[i].equals("RIGGED")) {
                rigged = true;
            }
        }
        HostApp app = new HostApp(rigged, players);
        app.start();
    }

    public HostApp(boolean riggingEnabled, int maxPlayers) {
        this.riggingEnabled = riggingEnabled;
        MAX_PLAYERS = maxPlayers;

        // Create a game instance
        game = new Game(MAX_PLAYERS);
    }

    void start() throws IOException, InterruptedException {
        if (riggingEnabled) {
            ConsoleUtils.printSysMsg("RIGGING ENABLED");
        }

        host = new Player(new PlayerId(UUID.randomUUID().toString(), ConsoleUtils.userPrompt("Enter username to start server")));
        server = new Server(6794);

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
        game.endTurn(result);

        if (game.ended()) {
            ConsoleUtils.printPlayerScores(game.getPlayers());
            ConsoleUtils.printWinner(game.getWinner().username());
            gameEndLatch.countDown();
            server.stop();
            return;
        }

        ConsoleUtils.printPlayerScores(game.getPlayers());

        server.broadcast(new PlayerScoreMsg(new ArrayList<>(game.getPlayers())));

        if (game.isFinalRound()) {
            ConsoleUtils.printSysMsg("FINAL ROUND");
        }

        startTurn(game.startTurn());
    }

    void startTurn(PlayerId playerId) throws IOException {
        if (playerId == host.getPlayerId()) {
            PlayerTurn playerTurn = new PlayerTurn(game.getCurrentCard(), riggingEnabled);
            postTurn(playerTurn.start());
            return;
        }

        server.sendMsg(playerId.id(), new StartTurnMsg(game.getCurrentCard()));
    }
}
