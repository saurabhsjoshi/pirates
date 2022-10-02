package acceptance;

import org.joshi.pirates.Die;
import org.joshi.pirates.Turn;
import org.joshi.pirates.cards.FortuneCard;
import org.joshi.pirates.cards.SeaBattleCard;
import org.joshi.pirates.cards.SkullCard;
import org.joshi.pirates.ui.ConsoleUtils;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    private static int port = 6900;

    @BeforeEach
    void setup() throws IOException {
        ProcessBuilder builder = new ProcessBuilder(getJavaPath(), "-jar", "server.jar", "PLAYERS", "1", "RIGGED", "PORT", String.valueOf(port));
        builder.directory(new File(getCurrentPath()));
        server = builder.start();

        System.out.println("Test setup with port " + port);
        InputStream stdout = server.getInputStream();
        OutputStream stdin = server.getOutputStream();

        writer = new BufferedWriter(new OutputStreamWriter(stdin));
        reader = new BufferedReader(new InputStreamReader(stdout));
        String line = reader.readLine();

        while (line != null && !line.equals(ConsoleUtils.USER_PROMPT)) {
            line = reader.readLine();
        }

        writer.write("Player1");
        writer.newLine();
        writer.flush();
    }

    @AfterEach
    void teardown() {
        server.destroy();
        port++;
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
    void R61() throws IOException {
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

    @DisplayName("R62: score set of 6 monkeys on first roll (SC 1100)")
    @Test
    void R62() throws IOException {
        defaultRiggedCard();

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(4, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(5, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(1100, getPlayerScore());

    }

    @DisplayName("R63: score set of 7 parrots on first roll (SC 2100)")
    @Test
    void R63() throws IOException {
        defaultRiggedCard();

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(1, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(3, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(2100, getPlayerScore());
    }

    @DisplayName("R64: score set of 8 coins on first roll (SC 5400)  seq of 8 + 9 coins(FC is coin) +  full chest")
    @Test
    void R64() throws IOException {
        defaultRiggedCard();

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(1, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(2, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(3, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(5, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(6, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(7, new Die(Die.Side.GOLD_COIN))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(5400, getPlayerScore());
    }

    @DisplayName("R65: score set of 8 coins on first roll and FC is diamond (SC 5400)")
    @Test
    void R65() throws IOException {
        setRiggedFc(new FortuneCard(FortuneCard.Type.DIAMOND));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(1, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(2, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(3, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(5, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(6, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(7, new Die(Die.Side.GOLD_COIN))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(5400, getPlayerScore());
    }

    @DisplayName("R66: score set of 8 swords on first roll and FC is captain (SC 4500x2 = 9000) since full chest")
    @Test
    void R66() throws IOException {
        setRiggedFc(new FortuneCard(FortuneCard.Type.CAPTAIN));

        TestUtils.rigDice(reader, writer, List.of(
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
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(9000, getPlayerScore());
    }

    @DisplayName("R67: Score set of 8 monkeys over several rolls (SC 4600 because of FC is coin and full chest)")
    @Test
    void R67() throws IOException {
        defaultRiggedCard();

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(4, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(5, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(6, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(7, new Die(Die.Side.MONKEY))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(4600, getPlayerScore());
    }

    @DisplayName("R68: score a set of 2 diamonds over 2 rolls with FC is diamond (SC 400)")
    @Test
    void R68() throws IOException {
        setRiggedFc(new FortuneCard(FortuneCard.Type.DIAMOND));

        // First roll
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(3, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(6, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 0");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        // Second Roll
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(400, getPlayerScore());

    }

    @DisplayName("R69: score a set of 3 diamonds over 2 rolls (SC 500)")
    @Test
    void R69() throws IOException {
        defaultRiggedCard();

        // First roll
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(3, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(6, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 0 4");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        // Second Roll
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(500, getPlayerScore());

    }

    @DisplayName("R70: score a set of 3 coins over 2 rolls  (SC 600)")
    @Test
    void R70() throws IOException {
        defaultRiggedCard();

        // First roll
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(3, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(6, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 0 3");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        // Second Roll
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(1, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(600, getPlayerScore());
    }


    @DisplayName("R71: score a set of 3 coins over 2 rolls  with FC is diamond (SC 500)")
    @Test
    void R71() throws IOException {

        setRiggedFc(new FortuneCard(FortuneCard.Type.DIAMOND));

        // First roll
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(3, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(6, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 0 3");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        // Second Roll
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(1, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(500, getPlayerScore());
    }

    @DisplayName("R72: score a set of 4 monkeys and a set of 3 coins (including the COIN fortune card) (SC 600)")
    @Test
    void R72() throws IOException {
        defaultRiggedCard();

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(4, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(5, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(6, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(600, getPlayerScore());
    }

    @DisplayName("R77: roll 2 skulls, re roll one of them due to sorceress, then go to next round of turn")
    @Test
    void R77() throws IOException {
        setRiggedFc(new FortuneCard(FortuneCard.Type.SORCERESS));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(4, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(5, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(6, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(7, new Die(Die.Side.PARROT))
        ));

        // Activate one of the skull and re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "1 0");

        // Re-roll
        var lines = TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        boolean skullActivate = false;

        // Validate we did not see skull activation error
        for (var line : lines) {
            if (line.equals(ConsoleUtils.getSysMsg(ConsoleUtils.SKULL_ACTIVATE_MSG))) {
                skullActivate = true;
                break;
            }
        }

        assertFalse(skullActivate);
    }

    @DisplayName("R78: roll no skulls, then next round roll 1 skull and re-roll for it, then score")
    @Test
    void R78() throws IOException {
        setRiggedFc(new FortuneCard(FortuneCard.Type.SORCERESS));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(1, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(4, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(5, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(6, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(7, new Die(Die.Side.PARROT))
        ));

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(1, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(2, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(3, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(5, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(6, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(7, new Die(Die.Side.GOLD_COIN))
        ));

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 0 1 2 3 5 6");

        // Activate the skull
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "1 4");

        // Re-roll
        var lines = TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        boolean skullActivate = false;

        // Validate we did not see skull activation error
        for (var line : lines) {
            if (line.equals(ConsoleUtils.getSysMsg(ConsoleUtils.SKULL_ACTIVATE_MSG))) {
                skullActivate = true;
                break;
            }
        }

        assertFalse(skullActivate);

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(4, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(7, new Die(Die.Side.GOLD_COIN))
        ));

        //End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        int score = getPlayerScore();
        assertEquals(5300, score);

    }


    @DisplayName("R79: roll no skulls, then next round roll 1 skull and re-roll for it, then go to next round")
    @Test
    void R79() throws IOException {
        setRiggedFc(new FortuneCard(FortuneCard.Type.SORCERESS));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(1, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(4, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(5, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(6, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(7, new Die(Die.Side.PARROT))
        ));

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(1, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(2, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(3, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(5, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(6, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(7, new Die(Die.Side.GOLD_COIN))
        ));

        // Activate the skull
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "1 4");

        // Re-roll
        var lines = TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        boolean skullActivate = false;

        // Validate we did not see skull activation error
        for (var line : lines) {
            if (line.equals(ConsoleUtils.getSysMsg(ConsoleUtils.SKULL_ACTIVATE_MSG))) {
                skullActivate = true;
                break;
            }
        }
        assertFalse(skullActivate);
    }

    @DisplayName("R82: first roll gets 3 monkeys 3 parrots  1 skull 1 coin  SC = 1100  (i.e., sequence of of 6 + coin)")
    @Test
    void R82() throws IOException {
        setRiggedFc(new FortuneCard(FortuneCard.Type.MONKEY_BUSINESS));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(6, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(7, new Die(Die.Side.GOLD_COIN))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(1100, getPlayerScore());
    }

    @DisplayName("R83: over several rolls: 2 monkeys, 1 parrot, 2 coins, 1 diamond, 2 swords SC 400")
    @Test
    void R83() throws IOException {
        setRiggedFc(new FortuneCard(FortuneCard.Type.MONKEY_BUSINESS));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(6, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(7, new Die(Die.Side.GOLD_COIN))
        ));

        TestUtils.waitForUserPrompt(reader);
        // Monkey, Sword
        TestUtils.writeLine(writer, "2 0 1");

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(4, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.MONKEY))
        ));

        TestUtils.waitForUserPrompt(reader);

        // Diamond, Parrot, Monkey
        TestUtils.writeLine(writer, "2 3 6 7");

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(2, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(400, getPlayerScore());
    }

    @DisplayName("R84: over several rolls: 2 monkeys, 1 parrot, 2 coins, 1 diamond, 2 swords SC 400")
    @Test
    void R84() throws IOException {
        setRiggedFc(new FortuneCard(FortuneCard.Type.MONKEY_BUSINESS));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(1, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(6, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(7, new Die(Die.Side.GOLD_COIN))
        ));

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 0 1 3");

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.MONKEY))
        ));

        TestUtils.waitForUserPrompt(reader);

        // Diamond, Parrot, Monkey
        TestUtils.writeLine(writer, "2 2 4");

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(6, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(7, new Die(Die.Side.MONKEY))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(2000, getPlayerScore());
    }

    @DisplayName("R87: roll 3 parrots, 2 swords, 2 diamonds, 1 coin     put 2 diamonds and 1 coin in chest")
    @Test
    void R87() throws IOException {
        setRiggedFc(new FortuneCard(FortuneCard.Type.TREASURE_CHEST));
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(1, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(3, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(4, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(5, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(7, new Die(Die.Side.SWORD))
        ));

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 0 1 2");

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "4 3 4 5");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.PARROT))
        ));

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "4 0 1 2 6 7");

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "1 3 4 5");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(3, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(1100, getPlayerScore());

    }


    @DisplayName("R92: roll 2 skulls, 3 parrots, 3 coins   put 3 coins in chest")
    @Test
    void R92() throws IOException {
        setRiggedFc(new FortuneCard(FortuneCard.Type.TREASURE_CHEST));
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(3, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(6, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(7, new Die(Die.Side.GOLD_COIN))
        ));

        // Coins in chest
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "4 5 6 7");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(2, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(3, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(4, new Die(Die.Side.GOLD_COIN))
        ));

        // Coin in chest
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "4 4");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(2, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(3, new Die(Die.Side.GOLD_COIN))
        ));

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

        // Validate player dead
        assertTrue(playerDied);
        // Make sure correct score
        assertEquals(600, playerScore);
    }

    @DisplayName("R98: 3 monkeys, 3 swords, 1 diamond, 1 parrot FC: coin   => SC 400  (ie no bonus)")
    @Test
    void R98() throws IOException {
        defaultRiggedCard();

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(7, new Die(Die.Side.PARROT))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(400, getPlayerScore());
    }

    @DisplayName("R99: 3 monkeys, 3 swords, 2 coins FC: captain   => SC (100+100+200+500)*2 =  1800")
    @Test
    void R99() throws IOException {
        setRiggedFc(new FortuneCard(FortuneCard.Type.CAPTAIN));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(7, new Die(Die.Side.GOLD_COIN))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(1800, getPlayerScore());
    }

    @DisplayName("R100: 3 monkeys, 4 swords, 1 diamond, FC: coin   => SC 1000  (ie 100++200+100+100+bonus)")
    @Test
    void R100() throws IOException {
        defaultRiggedCard();

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(7, new Die(Die.Side.DIAMOND))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(1000, getPlayerScore());
    }

    @DisplayName("FC: 2 sword sea battle, first  roll:  4 monkeys, 1 sword, 2 parrots and a coin" +
            "then re-roll 2 parrots and get coin and 2nd sword" +
            " score is: 200 (coins) + 200 (monkeys) + 300 (swords of battle) + 500 (full chest) = 1200")
    @Test
    void R101() throws IOException {
        setRiggedFc(new SeaBattleCard(2));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.GOLD_COIN))
        ));

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 0 1 2 3 4 7");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(5, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(1200, getPlayerScore());

    }

    @DisplayName("R104: FC: monkey business and RTS: 2 monkeys, 1 parrot, 2 coins, 3 diamonds   SC 1200 (bonus)")
    @Test
    void R104() throws IOException {
        setRiggedFc(new FortuneCard(FortuneCard.Type.MONKEY_BUSINESS));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(3, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(6, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(7, new Die(Die.Side.DIAMOND))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(1200, getPlayerScore());
    }

    @DisplayName("R107: die by rolling one skull and having a FC with two skulls")
    @Test
    void R107() throws IOException {
        setRiggedFc(new SkullCard(2));
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(3, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(6, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(7, new Die(Die.Side.DIAMOND))
        ));
        validatePlayerDead();
    }

    @DisplayName("R108: die by rolling 2 skulls and having a FC with 1 skull")
    @Test
    void R108() throws IOException {
        setRiggedFc(new SkullCard(1));
        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(3, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(6, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(7, new Die(Die.Side.DIAMOND))
        ));
        validatePlayerDead();
    }

    @DisplayName("R109: roll 2 skulls AND have a FC with two skulls: roll 2 skulls next roll, then 1 skull => -700")
    @Test
    void R109() {
        Turn playerTurn = new Turn();
        playerTurn.setFortuneCard(new SkullCard(2));

        playerTurn.roll();
        playerTurn.setRiggedDice(List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(3, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(6, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(7, new Die(Die.Side.DIAMOND))));
        playerTurn.postRoll();

        // Validate on island of dead
        assertTrue(playerTurn.isOnIslandOfSkulls());

        playerTurn.roll();
        playerTurn.setRiggedDice(List.of(
                new Turn.RiggedDie(1, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(2, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(6, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(7, new Die(Die.Side.MONKEY))));
        playerTurn.postRoll();

        playerTurn.roll();
        playerTurn.setRiggedDice(List.of(
                new Turn.RiggedDie(1, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(4, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(7, new Die(Die.Side.MONKEY))));
        playerTurn.postRoll();

        var result = playerTurn.complete();
        assertTrue(result.islandOfDead());
        assertEquals(-700, result.score());
    }

    @DisplayName("R110: roll 3 skulls AND have a FC with two skulls: roll no skulls next roll  => -500")
    @Test
    void R110() {
        Turn playerTurn = new Turn();
        playerTurn.setFortuneCard(new SkullCard(2));

        playerTurn.roll();
        playerTurn.setRiggedDice(List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(3, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(6, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(7, new Die(Die.Side.DIAMOND))));
        playerTurn.postRoll();

        // Validate on island of dead
        assertTrue(playerTurn.isOnIslandOfSkulls());

        playerTurn.roll();
        playerTurn.setRiggedDice(List.of(
                new Turn.RiggedDie(1, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(6, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(7, new Die(Die.Side.MONKEY))));
        playerTurn.postRoll();

        var result = playerTurn.complete();
        assertTrue(result.islandOfDead());
        assertEquals(-500, result.score());
    }

    @DisplayName("R111: roll 3 skulls AND have a FC with 1 skull: roll 1 skull next roll then none => -500")
    @Test
    void R111() {
        Turn playerTurn = new Turn();
        playerTurn.setFortuneCard(new SkullCard(1));

        playerTurn.roll();
        playerTurn.setRiggedDice(List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(3, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(6, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(7, new Die(Die.Side.DIAMOND))));
        playerTurn.postRoll();

        // Validate on island of dead
        assertTrue(playerTurn.isOnIslandOfSkulls());

        playerTurn.roll();
        playerTurn.setRiggedDice(List.of(
                new Turn.RiggedDie(1, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(2, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(6, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(7, new Die(Die.Side.MONKEY))));
        playerTurn.postRoll();

        var result = playerTurn.complete();
        assertTrue(result.islandOfDead());
        assertEquals(-500, result.score());
    }


    @DisplayName("R114: FC 2 swords, have 1 sword and die on first roll with 3 skulls   => lose 300 points")
    @Test
    void R114() {
        Turn playerTurn = new Turn();
        playerTurn.setFortuneCard(new SeaBattleCard(2));

        playerTurn.roll();
        playerTurn.setRiggedDice(List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(3, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(7, new Die(Die.Side.DIAMOND))));
        playerTurn.postRoll();

        var result = playerTurn.complete();
        assertFalse(result.islandOfDead());
        assertEquals(-300, result.score());
    }

    @DisplayName("R115: FC 3 swords, have 2 swords, 2 skulls and 4 parrots, die on reroll of parrots  => lose 500 points")
    @Test
    void R115() {
        Turn playerTurn = new Turn();
        playerTurn.setFortuneCard(new SeaBattleCard(3));

        playerTurn.roll();
        playerTurn.setRiggedDice(List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(3, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.PARROT))));
        playerTurn.postRoll();
        playerTurn.hold(List.of(0, 1));

        playerTurn.roll();
        playerTurn.setRiggedDice(List.of(
                new Turn.RiggedDie(4, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(5, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.PARROT))));
        playerTurn.postRoll();

        var result = playerTurn.complete();
        assertFalse(result.islandOfDead());
        assertEquals(-500, result.score());
    }

    @DisplayName("R116: FC 4 swords, die on first roll with 3 skulls and 3 swords  => lose 1000 points")
    @Test
    void R116() {
        Turn playerTurn = new Turn();
        playerTurn.setFortuneCard(new SeaBattleCard(4));

        playerTurn.roll();
        playerTurn.setRiggedDice(List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(3, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(5, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.PARROT))));
        playerTurn.postRoll();

        var result = playerTurn.complete();
        assertFalse(result.islandOfDead());
        assertEquals(-1000, result.score());
    }

    @DisplayName("R117: show a deduction received from a sea battle cannot make your score negative")
    @Test
    void R117() throws IOException {
        setRiggedFc(new SeaBattleCard(4));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(3, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(4, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(5, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.PARROT))
        ));

        // Validates score is zero and not negative
        validatePlayerDead();
    }

    @DisplayName("R118: FC 2 swords, roll 3 monkeys 2 swords, 1 coin, 2 parrots  SC = 100 + 100 + 300 = 500")
    @Test
    void R118() throws IOException {
        setRiggedFc(new SeaBattleCard(2));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.GOLD_COIN)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.PARROT))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(500, getPlayerScore());
    }

    @DisplayName("R119: FC 2 swords, roll 4 monkeys 1 sword, 1 skull  2 parrots")
    @Test
    void R119() throws IOException {
        setRiggedFc(new SeaBattleCard(2));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.PARROT))
        ));

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 0 1 2 3 4");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(500, getPlayerScore());
    }

    @DisplayName("R121: FC 3 swords, roll 3 monkeys 4 swords  SC = 100 + 200 + 500 = 800")
    @Test
    void R121() throws IOException {
        setRiggedFc(new SeaBattleCard(3));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(800, getPlayerScore());
    }

    @DisplayName("R122: FC 3 swords, roll 4 monkeys 2 swords 2 skulls" +
            "then re-roll 4 monkeys and get  2 skulls and 2 swords   -> DIE\n")
    @Test
    void R122() throws IOException {
        setRiggedFc(new SeaBattleCard(3));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(4, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(5, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(6, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 4 5");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(1, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD))
        ));

        validatePlayerDead();
    }

    @DisplayName("R124: FC 4 swords, roll 3 monkeys 4 swords 1 skull  SC = 100 +200 + 1000 = 1300")
    @Test
    void R124() throws IOException {
        setRiggedFc(new SeaBattleCard(4));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(1, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(2, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(5, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(6, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(7, new Die(Die.Side.SKULL))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(1300, getPlayerScore());
    }


    @DisplayName("R125: FC 4 swords, roll 3 monkeys, 1 sword, 1 skull, 1 diamond, 2 parrots" +
            "then re-roll 2 parrots and get 2 swords thus you have 3 monkeys, 3 swords, 1 diamond, 1 skull" +
            "then re-roll 3 monkeys and get  1 sword and 2 parrots  SC = 200 + 100 + 1000 = 1300")
    @Test
    void R125() throws IOException {

        setRiggedFc(new SeaBattleCard(4));

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(1, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(2, new Die(Die.Side.MONKEY)),
                new Turn.RiggedDie(3, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(4, new Die(Die.Side.SKULL)),
                new Turn.RiggedDie(5, new Die(Die.Side.DIAMOND)),
                new Turn.RiggedDie(6, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(7, new Die(Die.Side.PARROT))
        ));

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 0 1 2 3 5");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(6, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(7, new Die(Die.Side.SWORD))
        ));

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "2 6 7");

        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "1 0 1 2");

        // Re-roll
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "3");

        TestUtils.rigDice(reader, writer, List.of(
                new Turn.RiggedDie(0, new Die(Die.Side.SWORD)),
                new Turn.RiggedDie(1, new Die(Die.Side.PARROT)),
                new Turn.RiggedDie(2, new Die(Die.Side.PARROT))
        ));

        // End turn
        TestUtils.waitForUserPrompt(reader);
        TestUtils.writeLine(writer, "0");

        assertEquals(1300, getPlayerScore());

    }


}