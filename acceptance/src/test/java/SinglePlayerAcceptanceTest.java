import org.joshi.pirates.Die;
import org.joshi.pirates.Turn;
import org.joshi.pirates.cards.FortuneCard;
import org.joshi.pirates.ui.ConsoleUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SinglePlayerAcceptanceTest {

    private Process server;

    BufferedWriter writer;

    BufferedReader reader;

    private String getJavaPath() {
        return ProcessHandle.current()
                .info()
                .command()
                .orElseThrow();
    }

    private String getCurrentPath() {
        return Path.of("").toAbsolutePath().toString();
    }

    @BeforeEach
    void setup() throws IOException {
        ProcessBuilder builder = new ProcessBuilder(getJavaPath(), "-jar", "server.jar", "PLAYERS", "1", "RIGGED");
        builder.directory(new File(getCurrentPath()));
        server = builder.start();
        InputStream stdout = server.getInputStream();
        OutputStream stdin = server.getOutputStream();

        writer = new BufferedWriter(new OutputStreamWriter(stdin));
        reader = new BufferedReader(new InputStreamReader(stdout));
        String line = reader.readLine();

        while (!line.equals(ConsoleUtils.USER_PROMPT)) {
            line = reader.readLine();
        }

        writer.write("Player1");
        writer.newLine();
        writer.flush();
    }

    @AfterEach
    void teardown() {
        server.destroy();
    }

    @DisplayName("R45: die with 3 skulls on first roll")
    @Test
    void DieWith3Skulls_45() throws IOException {
        // Wait for rigged card prompt
        TestUtils.waitForUserPrompt(reader);
        TestUtils.rigFortuneCard(writer, new FortuneCard(FortuneCard.Type.GOLD));

        // Wait for rigged dice prompt
        TestUtils.waitForUserPrompt(reader);
        TestUtils.rigDice(writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL, Die.State.ACTIVE)),
                new Turn.RiggedDie(1, new Die(Die.Side.SKULL, Die.State.ACTIVE)),
                new Turn.RiggedDie(2, new Die(Die.Side.SKULL, Die.State.ACTIVE)),
                new Turn.RiggedDie(3, new Die(Die.Side.DIAMOND, Die.State.ACTIVE)),
                new Turn.RiggedDie(4, new Die(Die.Side.DIAMOND, Die.State.ACTIVE)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND, Die.State.ACTIVE)),
                new Turn.RiggedDie(6, new Die(Die.Side.DIAMOND, Die.State.ACTIVE)),
                new Turn.RiggedDie(7, new Die(Die.Side.DIAMOND, Die.State.ACTIVE))
        ));

        // Wait for new round prompt
        var lines = TestUtils.waitForUserPrompt(reader);

        boolean playerDied = false;
        int playerScore = -1;

        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            if (line.equals(ConsoleUtils.getSysMsg(ConsoleUtils.DEAD_MSG))) {
                playerDied = true;
            } else if (line.equals(ConsoleUtils.getSysMsg(ConsoleUtils.SCORE_MSG))) {
                playerScore = TestUtils.getPlayerScore(lines.get(++i));
            }
        }

        assertTrue(playerDied);
        assertEquals(0, playerScore);
    }

    @DisplayName("R46: roll 1 skull, 4 parrots, 3 swords, hold parrots, re-roll 3 swords, get 2 skulls 1 sword  die")
    @Test
    void Row46() throws IOException {
        // Wait for rigged card prompt
        TestUtils.waitForUserPrompt(reader);
        TestUtils.rigFortuneCard(writer, new FortuneCard(FortuneCard.Type.GOLD));

        // Wait for rigged dice prompt
        TestUtils.waitForUserPrompt(reader);

        // 1 skull, 4 parrots, 3 swords
        TestUtils.rigDice(writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL, Die.State.ACTIVE)),
                new Turn.RiggedDie(1, new Die(Die.Side.PARROT, Die.State.ACTIVE)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT, Die.State.ACTIVE)),
                new Turn.RiggedDie(3, new Die(Die.Side.PARROT, Die.State.ACTIVE)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT, Die.State.ACTIVE)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD, Die.State.ACTIVE)),
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD, Die.State.ACTIVE)),
                new Turn.RiggedDie(7, new Die(Die.Side.SWORD, Die.State.ACTIVE))
        ));

        // Hold parrots
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 1 2 3 4");

        //re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        // Wait for rigged dice prompt
        TestUtils.waitForUserPrompt(reader);

        // get 2 skulls 1 sword
        TestUtils.rigDice(writer, List.of(
                new Turn.RiggedDie(5, new Die(Die.Side.SKULL, Die.State.ACTIVE)),
                new Turn.RiggedDie(6, new Die(Die.Side.SKULL, Die.State.ACTIVE)),
                new Turn.RiggedDie(7, new Die(Die.Side.SWORD, Die.State.ACTIVE))
        ));

        // Wait for new round prompt
        var lines = TestUtils.waitForUserPrompt(reader);

        boolean playerDied = false;
        int playerScore = -1;

        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            if (line.equals(ConsoleUtils.getSysMsg(ConsoleUtils.DEAD_MSG))) {
                playerDied = true;
            } else if (line.equals(ConsoleUtils.getSysMsg(ConsoleUtils.SCORE_MSG))) {
                playerScore = TestUtils.getPlayerScore(lines.get(++i));
            }
        }

        assertTrue(playerDied);
        assertEquals(0, playerScore);

    }


}
