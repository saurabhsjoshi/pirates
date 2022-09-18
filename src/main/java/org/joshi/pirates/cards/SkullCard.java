package org.joshi.pirates.cards;

public class SkullCard extends FortuneCard {
    private final int skulls;

    public SkullCard(int skulls) {
        super(Type.SKULLS);
        this.skulls = skulls;
    }
}
