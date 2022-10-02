package acceptance;

import org.joshi.pirates.Turn;
import org.joshi.pirates.cards.FortuneCard;
import org.joshi.pirates.cards.SeaBattleCard;
import org.joshi.pirates.cards.SkullCard;
import org.joshi.pirates.ui.ConsoleUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class TestUtils {

    public static void rigFortuneCard(BufferedWriter writer, FortuneCard fortuneCard) throws IOException {
        rigFortuneCard(writer, fortuneCard, null);
    }

    public static void rigFortuneCard(BufferedWriter writer, FortuneCard fortuneCard, Logger logger) throws IOException {
        String line;
        if (fortuneCard instanceof SeaBattleCard seaBattleCard) {
            line = FortuneCard.Type.SEA_BATTLE.ordinal() + " " + seaBattleCard.getSwords();
        } else if (fortuneCard instanceof SkullCard skullCard) {
            line = FortuneCard.Type.SKULLS.ordinal() + " " + skullCard.getSkulls();
        } else {
            line = String.valueOf(fortuneCard.getType().ordinal());
        }

        writeLine(writer, line, logger);
    }

    public static void rigDice(BufferedReader reader, BufferedWriter writer, List<Turn.RiggedDie> dice) throws IOException {
        rigDice(reader, writer, null, dice);
    }

    public static void rigDice(BufferedReader reader, BufferedWriter writer, Logger logger, List<Turn.RiggedDie> dice) throws IOException {
        waitForUserPrompt(reader, logger);

        StringJoiner joiner = new StringJoiner(" ");
        for (var die : dice) {
            joiner.add(String.valueOf(die.index()));
            var d = die.die();
            joiner.add(String.valueOf(d.getDiceSide().ordinal()));
            joiner.add(String.valueOf(d.getState().ordinal()));
        }

        writeLine(writer, joiner.toString(), logger);
    }


    public static List<String> waitForUserPrompt(BufferedReader reader) throws IOException {
        return waitForUserPrompt(reader, null);
    }

    public static List<String> waitForUserPrompt(BufferedReader reader, Logger logger) throws IOException {
        return waitForPrompt(reader, ConsoleUtils.USER_PROMPT, logger);
    }

    public static List<String> waitForEndTurn(BufferedReader reader, String playerName, Logger logger) throws IOException {
        return waitForPrompt(reader, ConsoleUtils.getEndTurnMsg(playerName), logger);
    }

    public static boolean validateWinner(BufferedReader reader, String playerName, Logger logger) throws IOException {
        waitForPrompt(reader, ConsoleUtils.getSysMsg(ConsoleUtils.WINNER_MSG), logger);
        String line = reader.readLine();
        if (line == null) {
            return false;
        }
        logger.push(line);
        return playerName.equals(line.split("\\s+")[1]);
    }

    public static Map<String, Integer> readScores(BufferedReader reader, Logger logger) throws IOException {
        var scores = new HashMap<String, Integer>();
        waitForPrompt(reader, ConsoleUtils.getSysMsg(ConsoleUtils.SCORE_MSG), logger);
        // Next three lines will be the scores
        for (int i = 0; i < 3; i++) {
            String line = reader.readLine();
            logger.push(line);
            var split = line.split("\\s+");
            scores.put(split[0], Integer.parseInt(split[1]));
        }
        return scores;
    }

    public static List<String> waitForPrompt(BufferedReader reader, String prompt, Logger logger) throws IOException {
        List<String> lines = new ArrayList<>();
        String line = reader.readLine();
        while (line != null && !line.equals(prompt)) {
            if (!line.isBlank()) {
                lines.add(line);

                if (logger != null) {
                    logger.push(line);
                } else {
                    System.out.println(line);
                }
            }
            line = reader.readLine();
        }

        if (logger != null) {
            logger.push(line);
        } else {
            System.out.println(line);
        }
        return lines;
    }

    public static void writeLine(BufferedWriter writer, String line) throws IOException {
        writeLine(writer, line, null);
    }

    public static void writeLine(BufferedWriter writer, String line, Logger logger) throws IOException {
        writer.write(line);
        writer.newLine();
        writer.flush();

        if (logger != null) {
            logger.push(line);
        } else {
            System.out.println(line);
        }
    }

    public static int getPlayerScore(String line) {
        return Integer.parseInt(line.split("\\s+")[1]);
    }
}
