package org.joshi.pirates.cards;

/**
 * A sea battle card.
 */
public class SeaBattleCard extends FortuneCard {

    /**
     * Number of swords on this card.
     */
    private final int swords;

    private final int bonus;

    public SeaBattleCard(int swords) {
        super(Type.SEA_BATTLE);
        this.swords = swords;

        switch (swords) {
            case 2 -> bonus = 300;
            case 3 -> bonus = 500;
            case 4 -> bonus = 1000;
            default -> bonus = 0;
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SeaBattleCard other) {
            return other.swords == this.swords && other.bonus == this.bonus;
        }
        return false;
    }

    public int getSwords() {
        return swords;
    }

    public int getBonus() {
        return bonus;
    }
}
