package acceptance;

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

    private void setRiggedFc(FortuneCard card) throws IOException {
        // Wait for rigged card prompt
        TestUtils.waitForUserPrompt(reader);
        TestUtils.rigFortuneCard(writer, card);
    }

    private void defaultRiggedCard() throws IOException {
        setRiggedFc(new FortuneCard(FortuneCard.Type.GOLD));
    }

    private int getPlayerScore() throws IOException {
        var lines = TestUtils.waitForUserPrompt(reader);
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            if (line.equals(ConsoleUtils.getSysMsg(ConsoleUtils.SCORE_MSG))) {
                return TestUtils.getPlayerScore(lines.get(++i));
            }
        }
        return -1;
    }

    /**
     * Common function used by multiple tests to validate that the player is dead and their score is zero.
     */
    private void validatePlayerDead() throws IOException {
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

    @DisplayName("R45: die with 3 skulls on first roll")
    @Test
    void DieWith3Skulls_45() throws IOException {
        defaultRiggedCard();

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(2, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(3, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(4, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(6, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(7, new Die(Die.Side.DIAMOND))
        ));

        validatePlayerDead();
    }

    @DisplayName("R46: roll 1 skull, 4 parrots, 3 swords, hold parrots, re-roll 3 swords, get 2 skulls 1 sword  die")
    @Test
    void Row46() throws IOException {
        defaultRiggedCard();

        // 1 skull, 4 parrots, 3 swords
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(3, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(7, new Die(Die.Side.SWORD))
        ));

        // Hold parrots
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 1 2 3 4");

        //re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        // get 2 skulls 1 sword
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(5, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(6, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(7, new Die(Die.Side.SWORD))
        ));

        validatePlayerDead();
    }

    @DisplayName("R47: roll 2 skulls, 4 parrots, 2 swords, hold parrots, re-roll swords, get 1 skull 1 sword  die")
    @Test
    void R47() throws IOException {
        defaultRiggedCard();

        // 2 skulls, 4 parrots, 2 swords
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(3, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(7, new Die(Die.Side.SWORD))
        ));

        // Hold parrots
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 2 3 4 5");

        //re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        // get 1 skull 1 sword
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(6, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(7, new Die(Die.Side.SWORD))
        ));

        validatePlayerDead();
    }

    @DisplayName("R48: roll 1 skull, 4 parrots, 3 swords, hold parrots, re-roll swords, get 1 skull 2 monkeys " +
            "re-roll 2 monkeys, get 1 skull 1 monkey and die")
    @Test
    void R48() throws IOException {
        defaultRiggedCard();

        // 1 skull, 4 parrots, 3 swords
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(3, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(7, new Die(Die.Side.SWORD))
        ));

        // Hold parrots
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 1 2 3 4");

        //re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        // get 1 skull 2 monkeys
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(5, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(6, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(7, new Die(Die.Side.MONKEY))
        ));

        //re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        // get 1 skull 1 monkey
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(6, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(7, new Die(Die.Side.MONKEY))
        ));

        validatePlayerDead();
    }

    @DisplayName("R50: roll 1 skull, 2 parrots, 3 swords, 2 coins, re-roll parrots get 2 coins" +
            "re-roll 3 swords, get 3 coins (SC 4000 for seq of 8 (with FC) + 8x100=800 = 4800)")
    @Test
    void R50() throws IOException {
        defaultRiggedCard();

        // 1 skull, 2 parrots, 3 swords, 2 coins
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(7, new Die(Die.Side.GOLD_COIN))
        ));

        // Hold all dice except parrots
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 3 4 5 6 7");

        //re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        // get 2 coins
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(1, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(2, new Die(Die.Side.GOLD_COIN))
        ));

        // Hold coins
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 1 2");

        // Set 3 swords to active for re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "1 3 4 5");

        //re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        // get 3 coins
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(3, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(5, new Die(Die.Side.GOLD_COIN))
        ));

        //End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        int score = getPlayerScore();
        assertEquals(4800, score);
    }

    @DisplayName("R52: score first roll with nothing but 2 diamonds and 2 coins and FC is captain (SC 800)")
    @Test
    void R52() throws IOException {
        setRiggedFc(new FortuneCard(FortuneCard.Type.CAPTAIN));

        // 2 diamonds and 2 coins
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(1, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(2, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(3, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // Hold diamonds and gold coins
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 0 1 2 3");

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        int score = getPlayerScore();
        assertEquals(800, score);
    }

    @DisplayName("R53: get set of 2 monkeys on first roll, get 3rd monkey on 2nd roll (SC 200 since FC is coin)")
    @Test
    void R53() throws IOException {
        defaultRiggedCard();

        // 2 monkeys
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(3, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(7, new Die(Die.Side.PARROT))
        ));

        // Hold monkeys
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 0 1");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        // get 3rd monkey
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(7, new Die(Die.Side.PARROT))
        ));

        //End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(200, getPlayerScore());
    }

    @DisplayName("R54: score 2 sets of 3 (monkey, swords) in RTS on first roll   (SC 300)")
    @Test
    void R54() throws IOException {
        defaultRiggedCard();

        // 2 sets of 3 (monkey, swords)
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(300, getPlayerScore());
    }

    @DisplayName("R55: score 2 sets of 3 (monkey, parrots) in RTS using 2 rolls (SC 300)")
    @Test
    void R55() throws IOException {
        defaultRiggedCard();
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // re roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        // 2 sets of 3 (monkey, swords)
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(300, getPlayerScore());
    }

    @DisplayName("R56: score a set of 3 diamonds correctly (i.e., 400 points)   (SC 500)")
    @Test
    void R56() throws IOException {
        defaultRiggedCard();

        // 3 diamonds
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(1, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(2, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(6, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        //End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(500, getPlayerScore());

    }

    @DisplayName("R57: score a set of 4 coins correctly (i.e., 200 + 400 points) with FC is a diamond (SC 700)")
    @Test
    void R57() throws IOException {
        setRiggedFc(new FortuneCard(FortuneCard.Type.DIAMOND));

        // 4 coins
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(1, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(2, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(3, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(6, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        //End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(700, getPlayerScore());
    }

    @DisplayName("R58: score set of 3 swords and set of 4 parrots correctly on first roll (SC 400 because of FC)")
    @Test
    void R58() throws IOException {
        defaultRiggedCard();

        // 3 swords and set of 4 parrots
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        //End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(400, getPlayerScore());
    }

    @DisplayName("R59: score set of 3 coins+ FC and set of 4 swords correctly over several rolls (SC = 200+400+200 = 800)")
    @Test
    void R59() throws IOException {
        defaultRiggedCard();

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(1, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // Hold coins and sword
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 0 1 2 3");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // Hold sword
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 4");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");


        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.MONKEY))
        ));

        // Hold sword
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 5");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(6, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        int score = getPlayerScore();
        assertEquals(800, score);
    }

    @DisplayName("R60: same as previous row but with captain fortune card  (SC = (100 + + 300 + 200)*2 = 1200)")
    @Test
    void R60() throws IOException {
        setRiggedFc(new FortuneCard(FortuneCard.Type.CAPTAIN));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(1, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.DIAMOND))
        ));

        // Hold coins and sword
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 0 1 2 3");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.MONKEY))
        ));

        // Hold sword
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 4");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");


        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.MONKEY))
        ));

        // Hold sword
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 5");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(6, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        int score = getPlayerScore();
        assertEquals(1200, score);
    }

    @DisplayName("R61: score set of 5 swords over 3 rolls (SC 600)")
    @Test
    void R61() throws IOException{
        defaultRiggedCard();

        // First roll
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(3, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(6, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 0 1");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        // Second Roll
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD))
        ));

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 2 6");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        // Third Roll
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(3, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD))
        ));

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 4");

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(600, getPlayerScore());

    }
}
