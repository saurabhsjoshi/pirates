package org.joshi.pirates;

import org.joshi.pirates.cards.FortuneCard;
import org.joshi.pirates.cards.SeaBattleCard;
import org.joshi.pirates.cards.SkullCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for each turn consisting of multiple rolls.
 */
public class TurnTest {

    private Turn turn;

    @BeforeEach
    void setup() {
        turn = new Turn();
    }

    @DisplayName("Validate first roll of a turn")
    @Test
    void testFirstRoll() {

        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.MONKEY, Die.State.ACTIVE)));
        }

        turn.setRiggedDice(riggedDice);
        turn.postRoll();

        var dice = turn.getDice();

        // Validate that eight die are present in a turn
        assertEquals(8, dice.size());

        // Validate all die are active
        for (var die : dice) {
            assertEquals(Die.State.ACTIVE, die.state);
        }
    }

    @DisplayName("Test that validates if the player can re-roll based on number of active die")
    @Test
    void testCanRoll_EnoughActiveDie() {

        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        // Rig the dice
        for (int i = 0; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.DIAMOND, Die.State.HELD)));
        }
        turn.setRiggedDice(riggedDice);

        turn.postRoll();

        assertEquals(Turn.State.NOT_ENOUGH_ACTIVE_DIE, turn.getState());

        turn.roll();

        riggedDice = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.DIAMOND, Die.State.ACTIVE)));
        }

        turn.setRiggedDice(riggedDice);
        turn.postRoll();

        assertEquals(turn.getState(), Turn.State.OK);
    }

    @DisplayName("Test that validates that player cannot re-roll after accumulating three skulls")
    @Test
    void testCanRoll_ThreeSkulls() {
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();

        // Rig the die
        for (int i = 0; i < 3; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.SKULL, Die.State.HELD)));
        }
        for (int i = 3; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.DIAMOND, Die.State.HELD)));
        }

        turn.setRiggedDice(riggedDice);
        turn.postRoll();

        assertEquals(turn.getState(), Turn.State.DISQUALIFIED);
    }

    @DisplayName("Test that validates that player cannot re-roll after accumulating three skulls via skulls card")
    @Test
    void testCanRoll_ThreeSkulls_SkullsCard() {
        turn.setFortuneCard(new SkullCard(1));
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();

        // Rig the die
        for (int i = 0; i < 2; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.SKULL, Die.State.HELD)));
        }
        for (int i = 2; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.DIAMOND, Die.State.HELD)));
        }

        turn.setRiggedDice(riggedDice);
        turn.postRoll();

        assertEquals(turn.getState(), Turn.State.DISQUALIFIED);
    }

    @DisplayName("Test that validates that player goes to island of skulls on their first roll of four skulls")
    @Test
    void testSkullIsland() {
        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.GOLD));
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        // Rig the die
        for (int i = 0; i < 4; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.SKULL, Die.State.HELD)));
        }
        for (int i = 4; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.DIAMOND, Die.State.HELD)));
        }
        turn.setRiggedDice(riggedDice);
        turn.postRoll();
        assertTrue(turn.onSkullIsland());

        // Ensure that player remains on island of skulls
        turn.roll();
        turn.postRoll();
        assertTrue(turn.onSkullIsland());

        // Forcefully remove player from island of skulls for second roll
        turn.setOnIslandOfSkulls(false);

        turn.roll();
        turn.postRoll();
        // Even with four skulls, the player should not reach island of skulls
        assertFalse(turn.onSkullIsland());
    }

    @DisplayName("Test that validates that player goes to island of skulls on their first roll with skulls card")
    @Test
    void testSkullIsland_SkullsCard() {
        turn.setFortuneCard(new SkullCard(2));
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.SKULL, Die.State.HELD)));
        }

        for (int i = 2; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.MONKEY, Die.State.HELD)));
        }

        turn.setRiggedDice(riggedDice);
        turn.postRoll();


        assertTrue(turn.onSkullIsland());
    }

    @DisplayName("Validate that re roll works as expected")
    @Test
    void testReRoll() {
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.DIAMOND, Die.State.HELD)));
        }
        for (int i = 5; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.GOLD_COIN, Die.State.ACTIVE)));
        }

        turn.setRiggedDice(riggedDice);
        turn.postRoll();
        turn.roll();

        var dice = turn.getDice();

        // Total number of die should not change
        assertEquals(8, dice.size());


        // Ensure held die have not changed
        for (int i = 0; i < 5; i++) {
            assertSame(dice.get(i), riggedDice.get(i).die());
        }

        for (int i = 5; i < 8; i++) {
            assertNotSame(dice.get(i), riggedDice.get(i).die());
        }
    }

    @DisplayName("Validate end of the turn score calculation with captain card")
    @Test
    void testEndTurn_CaptainCard() {

        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.CAPTAIN));
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.MONKEY, Die.State.ACTIVE)));
        }
        turn.setRiggedDice(riggedDice);
        turn.postRoll();


        var score = turn.complete().score();

        // 8 x monkey 4000 + 500 full chest * 2 captain
        assertEquals(9000, score);
    }

    @DisplayName("Validate end of the turn score calculation with gold card")
    @Test
    void testEndTurn_Gold() {

        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.GOLD));
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        riggedDice.add(new Turn.RiggedDie(0, new Die(Die.Side.MONKEY, Die.State.HELD)));

        for (int i = 1; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.GOLD_COIN, Die.State.HELD)));
        }
        turn.setRiggedDice(riggedDice);
        turn.postRoll();

        var score = turn.complete().score();

        // 8 of a kind + bonus for each gold coin
        assertEquals(4800, score);
    }

    @DisplayName("Validate end of the turn score calculation with diamond card")
    @Test
    void testEndTurn_Diamond() {

        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.DIAMOND));
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        riggedDice.add(new Turn.RiggedDie(0, new Die(Die.Side.MONKEY, Die.State.HELD)));

        for (int i = 1; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.DIAMOND, Die.State.HELD)));
        }

        turn.setRiggedDice(riggedDice);
        turn.postRoll();


        var score = turn.complete().score();

        // 8 of a kind + bonus for each diamond coin
        assertEquals(4800, score);
    }

    @DisplayName("Validate end of the turn score calculation with monkey business card")
    @Test
    void testEndTurn_Monkey() {
        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.MONKEY_BUSINESS));
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        riggedDice.add(new Turn.RiggedDie(0, new Die(Die.Side.PARROT, Die.State.HELD)));
        for (int i = 1; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.MONKEY, Die.State.HELD)));
        }

        turn.setRiggedDice(riggedDice);
        turn.postRoll();

        var score = turn.complete().score();

        // 8 of a kind + 500 full chest
        assertEquals(4500, score);
    }

    @DisplayName("Validate end of the turn score calculation for when player is on island of skulls")
    @Test
    void testEndTurn_IslandOfSkulls() {
        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.MONKEY_BUSINESS));
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        riggedDice.add(new Turn.RiggedDie(0, new Die(Die.Side.PARROT, Die.State.HELD)));
        for (int i = 1; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.SKULL, Die.State.ACTIVE)));
        }
        turn.setRiggedDice(riggedDice);
        turn.postRoll();

        var score = turn.complete().score();

        // 7 skulls (7 X 100)
        assertEquals(-700, score);
    }

    @DisplayName("Validate end of score calculation when player is dead")
    @Test
    void testScore_Dead() {
        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.GOLD));
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.SKULL, Die.State.HELD)));
        }
        for (int i = 3; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.DIAMOND, Die.State.HELD)));
        }
        turn.setRiggedDice(riggedDice);
        turn.postRoll();

        var score = turn.complete().score();

        assertEquals(0, score);
    }

    @DisplayName("Validated that when user marks dice for hold they are set to held state")
    @Test
    void testHold() {
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.GOLD_COIN, Die.State.ACTIVE)));
        }

        turn.setRiggedDice(riggedDice);
        turn.postRoll();
        turn.hold(List.of(0, 1, 2));

        var dice = turn.getDice();

        for (int i = 0; i < 3; i++) {
            assertEquals(dice.get(i).state, Die.State.HELD);
        }

        for (int i = 3; i < dice.size(); i++) {
            assertEquals(dice.get(i).state, Die.State.ACTIVE);
        }
    }

    @DisplayName("Validated that when user marks dice for active they are set to active state unless they are skulls")
    @Test
    void testActive_Skulls() {

        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        riggedDice.add(new Turn.RiggedDie(0, new Die(Die.Side.SKULL, Die.State.HELD)));
        for (int i = 1; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.GOLD_COIN, Die.State.HELD)));
        }

        turn.setRiggedDice(riggedDice);
        turn.postRoll();

        assertThrows(Turn.SkullActivatedException.class, () -> turn.active(List.of(0, 1, 2)));
        assertDoesNotThrow(() -> turn.active(List.of(1, 2)));

        for (int i = 1; i < 3; i++) {
            assertEquals(turn.getDice().get(i).state, Die.State.ACTIVE);
        }
    }

    @DisplayName("Validate activation of one skull using sorceress card.")
    @Test
    void testActive_Sorceress() {
        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.SORCERESS));
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        riggedDice.add(new Turn.RiggedDie(0, new Die(Die.Side.SKULL, Die.State.HELD)));
        riggedDice.add(new Turn.RiggedDie(1, new Die(Die.Side.SKULL, Die.State.HELD)));
        for (int i = 2; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.GOLD_COIN, Die.State.HELD)));
        }
        turn.setRiggedDice(riggedDice);
        turn.postRoll();

        // Cannot activate more than one skull
        assertThrows(Turn.SkullActivatedException.class, () -> turn.active(List.of(0, 1)));

        // Allow activation one skull
        assertDoesNotThrow(() -> turn.active(List.of(0)));

        // Should not be allowed to activate it again
        assertThrows(Turn.SkullActivatedException.class, () -> turn.active(List.of(1)));
    }

    @DisplayName("Validate that post roll skulls are marked as being held")
    @Test
    void validatePostRollSkullCheck() {
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        riggedDice.add(new Turn.RiggedDie(0, new Die(Die.Side.SKULL, Die.State.ACTIVE)));
        for (int i = 1; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.PARROT, Die.State.ACTIVE)));
        }

        turn.setRiggedDice(riggedDice);
        turn.postRoll();

        assertEquals(turn.getDice().get(0).state, Die.State.HELD);
    }

    @DisplayName("Validate player cannot go to Island of the Dead when in Sea Battle")
    @Test
    void testSeaBattle_IslandOfDead() {
        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        turn.setFortuneCard(new SeaBattleCard(2));
        turn.roll();
        for (int i = 0; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.SKULL, Die.State.ACTIVE)));
        }
        turn.setRiggedDice(riggedDice);
        assertFalse(turn.onSkullIsland());
    }

    @DisplayName("Validate bonus is received with Sea Battle")
    @Test
    void testSeaBattle_Bonus() {

        turn.setFortuneCard(new SeaBattleCard(2));
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        riggedDice.add(new Turn.RiggedDie(0, new Die(Die.Side.SWORD, Die.State.ACTIVE)));
        riggedDice.add(new Turn.RiggedDie(1, new Die(Die.Side.SWORD, Die.State.ACTIVE)));
        for (int i = 2; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.PARROT, Die.State.ACTIVE)));
        }
        turn.setRiggedDice(riggedDice);
        turn.postRoll();

        var score = turn.complete().score();

        // 6 of a kind 1000 + sea battle bonus of 300 + 500 treasure chest
        assertEquals(1800, score);
    }

    @DisplayName("Validate player score is zero if Sea Battle fails")
    @Test
    void testSeaBattle_Fail() {
        var seaCard = new SeaBattleCard(3);
        turn.setFortuneCard(seaCard);
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        riggedDice.add(new Turn.RiggedDie(0, new Die(Die.Side.SWORD, Die.State.ACTIVE)));
        riggedDice.add(new Turn.RiggedDie(1, new Die(Die.Side.SWORD, Die.State.ACTIVE)));
        for (int i = 2; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.PARROT, Die.State.ACTIVE)));
        }

        turn.setRiggedDice(riggedDice);
        turn.postRoll();

        var score = turn.complete().score();

        assertEquals(-seaCard.getBonus(), score);
    }

    @DisplayName("Validate die in treasure chest are not re-rolled")
    @Test
    void testTreasureChest() {

        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.TREASURE_CHEST));
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.PARROT, Die.State.ACTIVE)));
        }

        turn.setRiggedDice(riggedDice);
        turn.addToChest(List.of(0, 1, 2));
        turn.postRoll();
        turn.roll();

        //Validate that protected die are not re-rolled
        var dice = turn.getDice();

        for (int i = 0; i < 3; i++) {
            var die = dice.get(i);
            assertEquals(die.getDiceSide(), Die.Side.PARROT);
            assertEquals(Die.State.IN_TREASURE_CHEST, die.state);
        }
    }

    @DisplayName("Validate die in treasure chest are calculated even if player is dead")
    @Test
    void validateTreasureChestScore() {
        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.TREASURE_CHEST));

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.PARROT, Die.State.ACTIVE)));
        }
        for (int i = 5; i < 8; i++) {
            riggedDice.add(new Turn.RiggedDie(i, new Die(Die.Side.SKULL, Die.State.ACTIVE)));
        }

        turn.roll();
        turn.setRiggedDice(riggedDice);
        turn.addToChest(List.of(0, 1, 2));
        turn.postRoll();


        var score = turn.complete().score();

        // Still should score for three parrots (100)
        assertEquals(100, score);
    }

    @DisplayName("Validate full chest and captain card")
    @Test
    void testFullChest() {
        turn.setFortuneCard(new FortuneCard(FortuneCard.Type.CAPTAIN));
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        riggedDice.add(new Turn.RiggedDie(0, new Die(Die.Side.MONKEY, Die.State.HELD)));
        riggedDice.add(new Turn.RiggedDie(1, new Die(Die.Side.MONKEY, Die.State.HELD)));
        riggedDice.add(new Turn.RiggedDie(2, new Die(Die.Side.MONKEY, Die.State.HELD)));
        riggedDice.add(new Turn.RiggedDie(3, new Die(Die.Side.SWORD, Die.State.HELD)));
        riggedDice.add(new Turn.RiggedDie(4, new Die(Die.Side.SWORD, Die.State.HELD)));
        riggedDice.add(new Turn.RiggedDie(5, new Die(Die.Side.SWORD, Die.State.HELD)));
        riggedDice.add(new Turn.RiggedDie(6, new Die(Die.Side.GOLD_COIN, Die.State.HELD)));
        riggedDice.add(new Turn.RiggedDie(7, new Die(Die.Side.GOLD_COIN, Die.State.HELD)));

        turn.setRiggedDice(riggedDice);
        turn.postRoll();

        var score = turn.complete().score();

        // 3 x monkeys, 3 x swords (200)  2 x gold coins (200) full chest (500) * 2 captain
        assertEquals(score, 1800);
    }


    @DisplayName("Validate full chest with 2 sword sea battle")
    @Test
    void testFullChest_SeaBattle() {

        turn.setFortuneCard(new SeaBattleCard(2));
        turn.roll();

        List<Turn.RiggedDie> riggedDice = new ArrayList<>();
        riggedDice.add(new Turn.RiggedDie(0, new Die(Die.Side.SWORD, Die.State.HELD)));
        riggedDice.add(new Turn.RiggedDie(1, new Die(Die.Side.SWORD, Die.State.HELD)));
        riggedDice.add(new Turn.RiggedDie(2, new Die(Die.Side.MONKEY, Die.State.HELD)));
        riggedDice.add(new Turn.RiggedDie(3, new Die(Die.Side.MONKEY, Die.State.HELD)));
        riggedDice.add(new Turn.RiggedDie(4, new Die(Die.Side.MONKEY, Die.State.HELD)));
        riggedDice.add(new Turn.RiggedDie(5, new Die(Die.Side.MONKEY, Die.State.HELD)));
        riggedDice.add(new Turn.RiggedDie(6, new Die(Die.Side.GOLD_COIN, Die.State.HELD)));
        riggedDice.add(new Turn.RiggedDie(7, new Die(Die.Side.GOLD_COIN, Die.State.HELD)));

        turn.setRiggedDice(riggedDice);
        turn.postRoll();

        var score = turn.complete().score();

        // 4 x monkeys (200), 2 x gold coins (200), 2 x sword sea battle (300) + full chest (500)
        assertEquals(1200, score);
    }
}
