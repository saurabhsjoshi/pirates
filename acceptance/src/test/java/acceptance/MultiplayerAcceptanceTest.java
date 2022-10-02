package acceptance;

import org.joshi.pirates.Die;
import org.joshi.pirates.Turn;
import org.joshi.pirates.cards.FortuneCard;
import org.joshi.pirates.cards.SkullCard;
import org.joshi.pirates.ui.ConsoleUtils;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiplayerAcceptanceTest {
    private static final String player1Name = "Player1";
    private static final String player2Name = "Player2";
    private static final String player3Name = "Player3";

    private Process server, player2, player3;

    BufferedWriter writer1, writer2, writer3;
    BufferedReader reader1, reader2, reader3;

    private Logger logger;
    private Thread loggerThread;

    private String getJavaPath() {
        return ProcessHandle.current()
                .info()
                .command()
                .orElseThrow();
    }

    private String getCurrentPath() {
        return Path.of("").toAbsolutePath().toString();
    }

    private Process startApp(String jarName, int port) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(getJavaPath(), "-jar", jarName, "RIGGED", "PORT", String.valueOf(port));
        builder.directory(new File(getCurrentPath()));
        return builder.start();
    }

    private void validatePlayerDead(BufferedReader reader, String playerName) throws IOException {
        var lines = TestUtils.waitForEndTurn(reader, playerName, logger);

        boolean playerDied = false;

        for (String line : lines) {
            if (line.equals(ConsoleUtils.getSysMsg(ConsoleUtils.DEAD_MSG))) {
                playerDied = true;
                break;
            }
        }

        assertTrue(playerDied);
    }


    @BeforeEach
    void setup(TestInfo testInfo) throws IOException {
        var tags = testInfo.getTags().toArray(String[]::new);
        logger = new Logger(tags[0] + ".txt");
        loggerThread = new Thread(logger);
        loggerThread.start();

        int testPort = Integer.parseInt(tags[1]);

        server = startApp("server.jar", testPort);
        writer1 = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
        reader1 = new BufferedReader(new InputStreamReader(server.getInputStream()));
        TestUtils.waitForUserPrompt(reader1, logger);
        TestUtils.writeLine(writer1, player1Name, logger);

        player2 = startApp("client.jar", testPort);
        writer2 = new BufferedWriter(new OutputStreamWriter(player2.getOutputStream()));
        reader2 = new BufferedReader(new InputStreamReader(player2.getInputStream()));
        TestUtils.waitForUserPrompt(reader2, logger);
        TestUtils.writeLine(writer2, player2Name, logger);

        player3 = startApp("client.jar", testPort);
        writer3 = new BufferedWriter(new OutputStreamWriter(player3.getOutputStream()));
        reader3 = new BufferedReader(new InputStreamReader(player3.getInputStream()));
        TestUtils.waitForUserPrompt(reader3, logger);
        TestUtils.writeLine(writer3, player3Name, logger);
    }

    @AfterEach
    void teardown() {
        if (server != null)
            server.destroy();
        if (player2 != null)
            player2.destroy();
        if (player3 != null)
            player3.destroy();

        logger.stop();
        loggerThread.interrupt();
        try {
            loggerThread.join();
        } catch (InterruptedException ignore) {
        }
    }

    private void setRiggedFc(BufferedReader reader, BufferedWriter writer, FortuneCard card) throws IOException {
        // Wait for rigged card prompt
        TestUtils.waitForUserPrompt(reader, logger);
        TestUtils.rigFortuneCard(writer, card, logger);
    }

    @Tag("R131")
    @Tag("6795")
    @Timeout(value = 25)
    @Test
    void R131() throws IOException {

        setRiggedFc(reader1, writer1, new FortuneCard(FortuneCard.Type.CAPTAIN));
        // player1 rolls 7 swords + 1 skull
        TestUtils.rigDice(reader1, writer1, logger, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader1, logger);
        TestUtils.writeLine(writer1, "0", logger);

        TestUtils.waitForEndTurn(reader1, player1Name, logger);

        var scores = TestUtils.readScores(reader1, logger);

        assertEquals(4000, scores.get(player1Name));
        assertEquals(0, scores.get(player2Name));
        assertEquals(0, scores.get(player3Name));

        //player2 scores a set of 3
        setRiggedFc(reader2, writer2, new FortuneCard(FortuneCard.Type.SORCERESS));
        TestUtils.rigDice(reader2, writer2, logger, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(6, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader2, logger);
        TestUtils.writeLine(writer2, "0", logger);

        scores = TestUtils.readScores(reader1, logger);

        assertEquals(4000, scores.get(player1Name));
        assertEquals(100, scores.get(player2Name));
        assertEquals(0, scores.get(player3Name));

        //player3 scores 0
        setRiggedFc(reader3, writer3, new FortuneCard(FortuneCard.Type.CAPTAIN));
        TestUtils.rigDice(reader3, writer3, logger, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(6, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // Validate that the player was disqualified
        validatePlayerDead(reader3, player3Name);

        scores = TestUtils.readScores(reader1, logger);

        assertEquals(4000, scores.get(player1Name));
        assertEquals(100, scores.get(player2Name));
        assertEquals(0, scores.get(player3Name));

        // Validate player 1 wins
        assertTrue(TestUtils.validateWinner(reader1, player1Name, logger));
    }

    @Tag("R134")
    @Tag("6796")
    @Timeout(value = 25)
    @Test
    void R134() throws IOException {

        // player1 rolls 6 swords + 2 skulls with FC = captain
        setRiggedFc(reader1, writer1, new FortuneCard(FortuneCard.Type.CAPTAIN));
        TestUtils.rigDice(reader1, writer1, logger, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // End turn for player 1
        TestUtils.waitForUserPrompt(reader1, logger);
        TestUtils.writeLine(writer1, "0", logger);

        TestUtils.waitForEndTurn(reader1, player1Name, logger);

        var scores = TestUtils.readScores(reader1, logger);

        assertEquals(4000, scores.get(player1Name));
        assertEquals(0, scores.get(player2Name));
        assertEquals(0, scores.get(player3Name));

        // Player 2 scores 0
        setRiggedFc(reader2, writer2, new FortuneCard(FortuneCard.Type.CAPTAIN));
        TestUtils.rigDice(reader2, writer2, logger, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(6, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // Validate that the player2 was disqualified
        validatePlayerDead(reader2, player2Name);

        scores = TestUtils.readScores(reader1, logger);

        assertEquals(4000, scores.get(player1Name));
        assertEquals(0, scores.get(player2Name));
        assertEquals(0, scores.get(player3Name));

        // player3 rolls 4 skulls AND has FC  1 skull
        setRiggedFc(reader3, writer3, new SkullCard(1));
        TestUtils.rigDice(reader3, writer3, logger, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(6, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // End turn for player 1
        TestUtils.waitForUserPrompt(reader3, logger);
        TestUtils.writeLine(writer3, "0", logger);

        TestUtils.waitForEndTurn(reader3, player3Name, logger);

        scores = TestUtils.readScores(reader1, logger);

        assertEquals(3500, scores.get(player1Name));
        assertEquals(0, scores.get(player2Name));
        assertEquals(0, scores.get(player3Name));
    }

    @Tag("R139")
    @Tag("6797")
    @Timeout(value = 25)
    @Test
    void R139() throws IOException {
        //player 1 scores 0
        setRiggedFc(reader1, writer1, new FortuneCard(FortuneCard.Type.GOLD));
        TestUtils.rigDice(reader1, writer1, logger, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));
        validatePlayerDead(reader1, player1Name);

        var scores = TestUtils.readScores(reader1, logger);

        assertEquals(0, scores.get(player1Name));
        assertEquals(0, scores.get(player2Name));
        assertEquals(0, scores.get(player3Name));


        //player2 rolls 7 swords + 1 skull with FC = captain (4000 points - could win)
        setRiggedFc(reader2, writer2, new FortuneCard(FortuneCard.Type.CAPTAIN));
        TestUtils.rigDice(reader2, writer2, logger, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // End player2 turn
        TestUtils.waitForUserPrompt(reader2, logger);
        TestUtils.writeLine(writer2, "0", logger);

        scores = TestUtils.readScores(reader1, logger);

        assertEquals(0, scores.get(player1Name));
        assertEquals(4000, scores.get(player2Name));
        assertEquals(0, scores.get(player3Name));

        //player3 scores 0
        setRiggedFc(reader3, writer3, new FortuneCard(FortuneCard.Type.GOLD));
        TestUtils.rigDice(reader3, writer3, logger, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(6, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // Validate that player3 was disqualified
        validatePlayerDead(reader3, player3Name);

        scores = TestUtils.readScores(reader1, logger);

        assertEquals(0, scores.get(player1Name));
        assertEquals(4000, scores.get(player2Name));
        assertEquals(0, scores.get(player3Name));

        setRiggedFc(reader1, writer1, new FortuneCard(FortuneCard.Type.CAPTAIN));
        // player 1 has FC = Captain rolls 8 swords gets 8000 points
        TestUtils.rigDice(reader1, writer1, logger, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(7, new Die(Die.Side.SWORD))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader1, logger);
        TestUtils.writeLine(writer1, "0", logger);
        TestUtils.waitForEndTurn(reader1, player1Name, logger);

        scores = TestUtils.readScores(reader1, logger);

        // 8 x sword = 4000 + 500 (full chest) * 2 captain = 9000
        assertEquals(9000, scores.get(player1Name));
        assertEquals(4000, scores.get(player2Name));
        assertEquals(0, scores.get(player3Name));

        // Validate player 1 wins
        assertTrue(TestUtils.validateWinner(reader1, player1Name, logger));
    }

    @Tag("R144")
    @Tag("6798")
    @Timeout(value = 25)
    @Test
    void R144() throws IOException {
        //Player 1 has 6 swords, 2 skulls and FC coin, scores 1100 points
        setRiggedFc(reader1, writer1, new FortuneCard(FortuneCard.Type.GOLD));
        TestUtils.rigDice(reader1, writer1, logger, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // End turn for player 1
        TestUtils.waitForUserPrompt(reader1, logger);
        TestUtils.writeLine(writer1, "0", logger);

        TestUtils.waitForEndTurn(reader1, player1Name, logger);

        var scores = TestUtils.readScores(reader1, logger);

        assertEquals(1100, scores.get(player1Name));
        assertEquals(0, scores.get(player2Name));
        assertEquals(0, scores.get(player3Name));

        //Player 2 has FC Sorceress and 7 skulls and a coin
        setRiggedFc(reader2, writer2, new FortuneCard(FortuneCard.Type.SORCERESS));
        TestUtils.rigDice(reader2, writer2, logger, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(2, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(3, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(5, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(6, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(7, new Die(Die.Side.GOLD_COIN))
        ));

        // uses sorceress to activate a skull
        TestUtils.waitForUserPrompt(reader2, logger);
        TestUtils.writeLine(writer2, "1 0", logger);

        // Re-roll
        TestUtils.waitForUserPrompt(reader2, logger);
        TestUtils.writeLine(writer2, "3", logger);

        TestUtils.rigDice(reader2, writer2, logger, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.GOLD_COIN))));

        // Re-roll
        TestUtils.waitForUserPrompt(reader2, logger);
        TestUtils.writeLine(writer2, "3", logger);

        TestUtils.rigDice(reader2, writer2, logger, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(7, new Die(Die.Side.SWORD))
        ));

        // End player2 turn
        TestUtils.waitForUserPrompt(reader2, logger);
        TestUtils.writeLine(writer2, "0", logger);

        scores = TestUtils.readScores(reader1, logger);

        assertEquals(300, scores.get(player1Name));
        assertEquals(0, scores.get(player2Name));
        assertEquals(0, scores.get(player3Name));
    }

}
