import org.joshi.pirates.Turn;
import org.joshi.pirates.cards.FortuneCard;
import org.joshi.pirates.cards.SeaBattleCard;
import org.joshi.pirates.cards.SkullCard;
import org.joshi.pirates.ui.ConsoleUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class TestUtils {

    public static void rigFortuneCard(BufferedWriter writer, FortuneCard fortuneCard) throws IOException {
        String line;
        if (fortuneCard instanceof SeaBattleCard seaBattleCard) {
            line = FortuneCard.Type.SEA_BATTLE.ordinal() + " " + seaBattleCard.getSwords();
        } else if (fortuneCard instanceof SkullCard skullCard) {
            line = FortuneCard.Type.SKULLS.ordinal() + " " + skullCard.getSkulls();
        } else {
            line = String.valueOf(fortuneCard.getType().ordinal());
        }
        writeLine(writer, line);
    }

    public static void rigDice(BufferedWriter writer, List<Turn.RiggedDie> dice) throws IOException {
        StringJoiner joiner = new StringJoiner(" ");
        for (var die : dice) {
            joiner.add(String.valueOf(die.index()));
            var d = die.die();
            joiner.add(String.valueOf(d.getDiceSide().ordinal()));
            joiner.add(String.valueOf(d.getState().ordinal()));
        }

        writeLine(writer, joiner.toString());
    }

    public static List<String> waitForUserPrompt(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList<>();
        String line = reader.readLine();
        while (!line.equals(ConsoleUtils.USER_PROMPT)) {
            if (!line.isBlank()) {
                lines.add(line);
                System.out.println(line);
            }
            line = reader.readLine();
        }
        return lines;
    }

    public static void writeLine(BufferedWriter writer, String line) throws IOException {
        writer.write(line);
        writer.newLine();
        writer.flush();
    }

    public static int getPlayerScore(String line) {
        return Integer.parseInt(line.split("\\s+")[1]);
    }
}
