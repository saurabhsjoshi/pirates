package org.joshi.pirates.cards;

/**
 * Class that represents a fortune card.
 */
public class FortuneCard {

    public enum Type {
        TREASURE_CHEST,
        CAPTAIN,
        SORCERESS,
        SEA_BATTLE,
        GOLD,
        DIAMOND,
        MONKEY_BUSINESS,
        SKULLS
    }

    private final Type type;

    public FortuneCard(Type type) {
        this.type = type;
    }
}
