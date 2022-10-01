package org.joshi.pirates.msg;

import org.joshi.network.Message;
import org.joshi.pirates.cards.FortuneCard;

public class StartTurnMsg extends Message {
    public static final String TYPE = "START_TURN";

    private final FortuneCard fortuneCard;

    public StartTurnMsg(FortuneCard fortuneCard) {
        this.fortuneCard = fortuneCard;
    }

    public FortuneCard getFortuneCard() {
        return fortuneCard;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
