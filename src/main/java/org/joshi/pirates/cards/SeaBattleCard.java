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

    public SeaBattleCard(int swords, int bonus) {
        super(Type.SEA_BATTLE);
        this.swords = swords;
        this.bonus = bonus;
    }
}
