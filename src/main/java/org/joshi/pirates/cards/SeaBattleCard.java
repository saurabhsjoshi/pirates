package org.joshi.pirates.cards;

/**
 * A sea battle card.
 */
public class SeaBattleCard extends FortuneCard {

    /**
     * Number of swords on this card.
     */
    private final int swords;

    public SeaBattleCard(int swords) {
        super(Type.SEA_BATTLE);
        this.swords = swords;
    }
}
