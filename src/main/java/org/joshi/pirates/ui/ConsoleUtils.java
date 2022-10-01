package org.joshi.pirates.ui;

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

    public static void startGameMsg() {
        printSysMsg("STARTING GAME");
    }

    private static void printSysMsg(String msg) {
        System.out.println(SYSTEM_MSG_SEPARATOR + msg + SYSTEM_MSG_SEPARATOR);
    }
}
