package org.joshi.pirates.cards;

import java.io.Serializable;

/**
 * Class that represents a fortune card.
 */
public class FortuneCard implements Serializable {

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

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FortuneCard other) {
            return other.type == this.type;
        }
        return false;
    }
}
