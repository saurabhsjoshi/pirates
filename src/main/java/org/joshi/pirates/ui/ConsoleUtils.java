package org.joshi.pirates.ui;

import org.joshi.pirates.Die;
import org.joshi.pirates.cards.FortuneCard;
import org.joshi.pirates.cards.SeaBattleCard;
import org.joshi.pirates.cards.SkullCard;

import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {
    private static final String USER_PROMPT = "##: ";

    private static final String SYSTEM_MSG_SEPARATOR = "######";

    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Prints a prompt for the user.
     *
     * @param prompt message to show
     * @return user response
     */
    public static String userPrompt(String prompt) {
        System.out.println(prompt);
        System.out.print(USER_PROMPT);
        return scanner.nextLine();
    }

    public static String printRoundOptions(FortuneCard card) {
        System.out.println("1. Set die as active");
        System.out.println("2. Set die as held");
        System.out.println("3. Re roll");

        if (card.getType() == FortuneCard.Type.TREASURE_CHEST) {
            System.out.println("4. Select die to put in treasure chest");
        }

        System.out.println("0. Complete turn");
        return userPrompt("What would you like to do?");
    }

    public static void startGameMsg() {
        printSysMsg("STARTING GAME");
        System.out.print("\n\n");
    }

    public static void startTurnMsg() {
        printSysMsg("STARTING YOUR TURN");
    }

    private static String fortuneCard(FortuneCard card) {
        if (card instanceof SeaBattleCard seaBattleCard) {
            return card.getType().name() + " (SWORDS:" + seaBattleCard.getSwords() + ", BONUS:" + seaBattleCard.getBonus() + ")";
        } else if (card instanceof SkullCard skullCard) {
            return card.getType().name() + " (" + skullCard.getSkulls() + ")";
        } else {
            return card.getType().name();
        }
    }

    public static void startRoundMsg(FortuneCard card) {
        System.out.println("\n\n\n");
        System.out.println("FORTUNE CARD: " + fortuneCard(card));
    }

    public static void printDice(List<Die> dice) {
        printSysMsg("DICE STATE");
        for (int i = 0; i < dice.size(); i++) {
            var die = dice.get(i);
            System.out.println(i + ":" + die.getDiceSide().name() + "(" + die.getState() + ")");
        }
    }

    public static void printSysMsg(String msg) {
        System.out.println(SYSTEM_MSG_SEPARATOR + msg + SYSTEM_MSG_SEPARATOR);
    }
}
