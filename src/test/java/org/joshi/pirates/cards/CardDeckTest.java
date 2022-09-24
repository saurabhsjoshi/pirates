package org.joshi.pirates.cards;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests that validate deck of fortune cards.
 */
public class CardDeckTest {
    CardDeck cardDeck;

    @Test
    @BeforeEach
    public void setup() {
        cardDeck = new CardDeck();
    }

    public static List<FortuneCard> getExpectedDeck() {
        List<FortuneCard> expectedCards = new ArrayList<>(35);

        // 4xChest, 4xSorceress, 4xCaptain, 4xMonkey&Parrot, 4xDiamond, 4xCoin
        for (int i = 0; i < 4; i++) {
            expectedCards.add(new FortuneCard(FortuneCard.Type.TREASURE_CHEST));
            expectedCards.add(new FortuneCard(FortuneCard.Type.SORCERESS));
            expectedCards.add(new FortuneCard(FortuneCard.Type.CAPTAIN));
            expectedCards.add(new FortuneCard(FortuneCard.Type.MONKEY_BUSINESS));
            expectedCards.add(new FortuneCard(FortuneCard.Type.DIAMOND));
            expectedCards.add(new FortuneCard(FortuneCard.Type.GOLD));
        }

        // 2x2skulls // 2x2swords(300 bonus) 2x3swords(500 bonus) 2x4swords(1000 bonus)
        for (int i = 0; i < 2; i++) {
            expectedCards.add(new SkullCard(2));
            expectedCards.add(new SeaBattleCard(2));
            expectedCards.add(new SeaBattleCard(3));
            expectedCards.add(new SeaBattleCard(4));
        }

        //3x1skull
        for (int i = 0; i < 3; i++) {
            expectedCards.add(new SkullCard(1));
        }

        return expectedCards;
    }

    @DisplayName("Test that validates the deck contains expected number of each card")
    @Test
    void testShuffle() {
        cardDeck.shuffle();
        var expectedCards = getExpectedDeck();

        while (!cardDeck.isEmpty()) {
            expectedCards.remove(cardDeck.top());
        }

        assertTrue(expectedCards.isEmpty());
    }
}
