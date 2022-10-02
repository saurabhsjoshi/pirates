package org.joshi.pirates.ui;

import org.joshi.pirates.Die;
import org.joshi.pirates.Turn;
import org.joshi.pirates.TurnResult;
import org.joshi.pirates.cards.FortuneCard;
import org.joshi.pirates.cards.SeaBattleCard;
import org.joshi.pirates.cards.SkullCard;

import java.util.ArrayList;
import java.util.List;

public class PlayerTurn {
    private FortuneCard fortuneCard;

    private final boolean rigged;

    public PlayerTurn(FortuneCard card, boolean rigged) {
        this.fortuneCard = card;
        this.rigged = rigged;
    }

    public TurnResult start() {
        Turn turn = new Turn();

        if (rigged) {
            String result = ConsoleUtils.userPrompt("Rig fortune card?");
            if (!result.isBlank()) {
                var split = result.split("\\s+");
                switch (split[0]) {
                    case "3" -> fortuneCard = new SeaBattleCard(Integer.parseInt(split[1]));
                    case "7" -> fortuneCard = new SkullCard(Integer.parseInt(split[1]));
                    default -> fortuneCard = new FortuneCard(FortuneCard.Type.values()[Integer.parseInt(split[0])]);
                }
            }
        }

        turn.setFortuneCard(fortuneCard);
        ConsoleUtils.startTurnMsg();

        while (true) {
            ConsoleUtils.startRoundMsg(fortuneCard);

            turn.roll();

            if (rigged) {
                String result = ConsoleUtils.userPrompt("Rig dice?");
                if (!result.isBlank()) {
                    List<Turn.RiggedDie> riggedDice = new ArrayList<>();

                    var split = result.split("\\s+");
                    for (int i = 0; i < split.length; i += 3) {
                        var idx = Integer.parseInt(split[i]);
                        var object = Die.Side.values()[Integer.parseInt(split[i + 1])];
                        var state = Die.State.values()[Integer.parseInt(split[i + 2])];
                        riggedDice.add(new Turn.RiggedDie(idx, new Die(object, state)));
                    }
                    turn.setRiggedDice(riggedDice);
                }
            }


            turn.postRoll();

            if (turn.isOnIslandOfSkulls()) {
                ConsoleUtils.printSysMsg("YOU ARE ON ISLAND OF DEAD");
            }

            boolean userOpt = false;

            while (!userOpt) {
                turn.updateState();
                ConsoleUtils.printDice(turn.getDice());

                if (turn.getState() == Turn.State.DISQUALIFIED) {
                    ConsoleUtils.printDeadMsg();
                    return turn.complete();
                }

                var result = ConsoleUtils.printRoundOptions(fortuneCard);
                if (result.isBlank()) {
                    continue;
                }

                var split = result.split("\\s+");

                List<Integer> index = new ArrayList<>();

                switch (result.charAt(0)) {

                    case '1':
                        for (int i = 1; i < split.length; i++) {
                            index.add(Integer.valueOf(split[i]));
                        }

                        try {
                            turn.active(index);
                        } catch (Turn.SkullActivatedException e) {
                            ConsoleUtils.printSkullActivated();
                        }

                        break;

                    case '2':
                        for (int i = 1; i < split.length; i++) {
                            index.add(Integer.valueOf(split[i]));
                        }
                        turn.hold(index);
                        break;

                    case '3':
                        if (turn.getState() == Turn.State.NOT_ENOUGH_ACTIVE_DIE) {
                            ConsoleUtils.printSysMsg("NOT ENOUGH ACTIVE DIE TO RE ROLL");
                        } else {
                            userOpt = true;
                        }
                        break;
                    case '4':
                        for (int i = 1; i < split.length; i++) {
                            index.add(Integer.valueOf(split[i]));
                        }
                        turn.addToChest(index);
                        break;
                    case '0':
                        return turn.complete();
                }
            }
        }
    }
}
