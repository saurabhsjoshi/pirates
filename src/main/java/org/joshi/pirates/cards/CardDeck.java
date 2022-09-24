package org.joshi.pirates.cards;

import java.util.Arrays;
import java.util.Collections;

public class CardDeck {
    private final FortuneCard[] cards;

    /**
     * Integer that keeps track of the current card number.
     */
    int current;

    public CardDeck() {
        this.cards = new FortuneCard[35];
    }

    private void populate() {
        // 4xChest, 4xSorceress, 4xCaptain, 4xMonkey&Parrot, 4xDiamond, 4xCoin
        for (int i = 0; i < 4; i++) {
            cards[i] = new FortuneCard(FortuneCard.Type.TREASURE_CHEST);
            cards[i + 4] = new FortuneCard(FortuneCard.Type.SORCERESS);
            cards[i + 8] = new FortuneCard(FortuneCard.Type.CAPTAIN);
            cards[i + 12] = new FortuneCard(FortuneCard.Type.MONKEY_BUSINESS);
            cards[i + 16] = new FortuneCard(FortuneCard.Type.DIAMOND);
            cards[i + 20] = new FortuneCard(FortuneCard.Type.GOLD);
        }

        // 2x2skulls 2x2swords(300 bonus) 2x3swords(500 bonus) 2x4swords(1000 bonus)
        for (int i = 24; i < 26; i++) {
            cards[i] = new SkullCard(2);
            cards[i + 2] = new SeaBattleCard(2);
            cards[i + 4] = new SeaBattleCard(3);
            cards[i + 6] = new SeaBattleCard(4);
        }

        for (int i = 32; i < 35; i++) {
            cards[i] = new SkullCard(1);
        }

        current = 34;
    }

    /**
     * Method that resets and shuffles this card deck instance.
     */
    public void shuffle() {
        populate();
        Collections.shuffle(Arrays.asList(cards));
    }

    /**
     * Retrieve card from top of the deck.
     */
    public FortuneCard top() {
        return cards[current--];
    }

    /**
     * Method that checks if the card deck is empty.
     */
    public boolean isEmpty() {
        return current < 0;
    }
}
