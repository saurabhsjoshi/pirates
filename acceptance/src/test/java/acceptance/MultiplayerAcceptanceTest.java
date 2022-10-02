package acceptance;

import org.joshi.pirates.Die;
import org.joshi.pirates.Turn;
import org.joshi.pirates.cards.FortuneCard;
import org.joshi.pirates.ui.ConsoleUtils;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

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

    private Process startApp(String jarName) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(getJavaPath(), "-jar", jarName, "RIGGED");
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
        logger = new Logger(testInfo.getTags().toArray(String[]::new)[0] + ".txt");
        loggerThread = new Thread(logger);
        loggerThread.start();

        server = startApp("server.jar");
        writer1 = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
        reader1 = new BufferedReader(new InputStreamReader(server.getInputStream()));
        TestUtils.waitForUserPrompt(reader1, logger);
        TestUtils.writeLine(writer1, player1Name, logger);

        player2 = startApp("client.jar");
        writer2 = new BufferedWriter(new OutputStreamWriter(player2.getOutputStream()));
        reader2 = new BufferedReader(new InputStreamReader(player2.getInputStream()));
        TestUtils.waitForUserPrompt(reader2, logger);
        TestUtils.writeLine(writer2, player2Name, logger);

        player3 = startApp("client.jar");
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

    private int getPlayerScore(BufferedReader reader, String playerName) throws IOException {
        var lines = TestUtils.waitForUserPrompt(reader);
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            if (line.equals(ConsoleUtils.getSysMsg(ConsoleUtils.SCORE_MSG))) {
                for (int j = i; j < 3; j++) {
                    var split = lines.get(j).split("\\s+");
                    if (split[0].equals(playerName)) {
                        return Integer.parseInt(split[1]);
                    }
                }
                break;
            }
        }
        return -1;
    }

    private void setRiggedFc(BufferedReader reader, BufferedWriter writer, FortuneCard card) throws IOException {
        // Wait for rigged card prompt
        TestUtils.waitForUserPrompt(reader, logger);
        TestUtils.rigFortuneCard(writer, card, logger);
    }

    @Tag("R131")
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

}
