package org.joshi.pirates.ui;

import org.joshi.network.MsgPublisher;
import org.joshi.pirates.Turn;
import org.joshi.pirates.TurnResult;
import org.joshi.pirates.cards.FortuneCard;

import java.util.ArrayList;
import java.util.List;

public class PlayerTurn {
    private final MsgPublisher publisher;
    private final FortuneCard fortuneCard;

    public PlayerTurn(MsgPublisher publisher, FortuneCard card) {
        this.publisher = publisher;
        this.fortuneCard = card;
    }

    public TurnResult start() {
        Turn turn = new Turn();
        turn.setFortuneCard(fortuneCard);

        ConsoleUtils.startTurnMsg();

        while (true) {
            ConsoleUtils.startRoundMsg(fortuneCard);

            turn.roll();
            turn.postRoll();

            if (turn.isOnIslandOfSkulls()) {
                ConsoleUtils.printSysMsg("YOU ARE ON ISLAND OF DEAD");
            }

            boolean userOpt = false;

            while (!userOpt) {
                turn.updateState();
                ConsoleUtils.printDice(turn.getDice());

                if (turn.getState() == Turn.State.DISQUALIFIED) {
                    ConsoleUtils.printSysMsg("YOU ARE DISQUALIFIED (3 SKULLS)");
                    return new TurnResult(false, 0);
                }

                var result = ConsoleUtils.printRoundOptions(fortuneCard);
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
                            ConsoleUtils.printSysMsg("CANNOT ACTIVATE SKULL");
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
