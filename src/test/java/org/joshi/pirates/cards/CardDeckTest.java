package org.joshi.pirates.cards;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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

    public static Set<FortuneCard> getExpectedDeck() {
        Set<FortuneCard> expectedCards = new HashSet<>();

        // Expect 3 treasure chest, captain and sorceress each
        for (int i = 0; i < 4; i++) {
            expectedCards.add(new FortuneCard(FortuneCard.Type.TREASURE_CHEST));
            expectedCards.add(new FortuneCard(FortuneCard.Type.CAPTAIN));
            expectedCards.add(new FortuneCard(FortuneCard.Type.SORCERESS));
        }

        // Expect 5 diamond, gold cards, monkey business each
        for (int i = 0; i < 6; i++) {
            expectedCards.add(new FortuneCard(FortuneCard.Type.DIAMOND));
            expectedCards.add(new FortuneCard(FortuneCard.Type.GOLD));
            expectedCards.add(new FortuneCard(FortuneCard.Type.MONKEY_BUSINESS));
        }

        for (int i = 0; i < 3; i++) {
            expectedCards.add(new SeaBattleCard(i + 2));
        }

        expectedCards.add(new SkullCard(1));
        expectedCards.add(new SkullCard(2));
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
