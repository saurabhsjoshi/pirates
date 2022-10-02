package org.joshi.pirates.cards;

public class SkullCard extends FortuneCard {
    private final int skulls;

    public SkullCard(int skulls) {
        super(Type.SKULLS);
        this.skulls = skulls;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SkullCard other) {
            return other.skulls == this.skulls;
        }
        return false;
    }

    public int getSkulls() {
        return skulls;
    }
}
