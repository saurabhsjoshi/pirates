package org.joshi.pirates.cards;

public class CardDeck {
    private final FortuneCard[] cards;

    public CardDeck() {
        this.cards = new FortuneCard[35];
    }

    /**
     * Method that resets and shuffles this card deck instance.
     */
    public void shuffle() {
        // TODO: Implement
    }

    /**
     * Retrieve card from top of the deck.
     */
    public FortuneCard top() {
        // TODO: Implement
        return new FortuneCard(FortuneCard.Type.DIAMOND);
    }

    /**
     * Method that checks if the card deck is empty.
     */
    public boolean isEmpty() {
        // TODO: Implement
        return true;
    }
}
