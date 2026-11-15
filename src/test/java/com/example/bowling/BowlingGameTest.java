package com.example.bowling;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BowlingGameTest {

    private BowlingGame game;

    @BeforeEach
    void setUp() {
        game = new BowlingGame();
    }

    private void rollMany(int n, int pins) {
        for (int i = 0; i < n; i++) {
            game.roll(pins);
        }
    }

    private void rollAll(int... pins) {
        for (int p : pins) {
            game.roll(p);
        }
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Functional correctness (F-xx)")
    class Functional {

        @Test
        @DisplayName("F-01 gutter game scores 0")
        void gutterGameScoresZero() {
            rollMany(20, 0);
            assertEquals(0, game.score());
        }

        @Test
        @DisplayName("F-02 all ones scores 20")
        void allOnesScoresTwenty() {
            rollMany(20, 1);
            assertEquals(20, game.score());
        }

        @Test
        @DisplayName("F-03 a spare adds the next roll as bonus")
        void oneSpareAddsNextRollBonus() {
            rollAll(5, 5, 3);
            rollMany(17, 0);
            assertEquals(16, game.score());
        }

        @Test
        @DisplayName("F-04 a strike adds the next two rolls as bonus")
        void oneStrikeAddsNextTwoRollsBonus() {
            rollAll(10, 3, 4);
            rollMany(16, 0);
            assertEquals(24, game.score());
        }

        @Test
        @DisplayName("F-05 all spares (5/5) scores 150")
        void allSparesScores150() {
            rollMany(21, 5);
            assertEquals(150, game.score());
        }

        @Test
        @DisplayName("F-06 perfect game scores 300")
        void perfectGameScores300() {
            rollMany(12, 10);
            assertTrue(game.isComplete());
            assertEquals(300, game.score());
        }

        @Test
        @DisplayName("F-07 typical mixed game scores 167")
        void typicalMixedGame() {
            // X 7/ 9- X -8 8/ -6 X X X81  => 167
            rollAll(10, 7, 3, 9, 0, 10, 0, 8, 8, 2, 0, 6, 10, 10, 10, 8, 1);
            assertEquals(167, game.score());
        }

        @Test
        @DisplayName("F-08 nine-and-miss in every frame scores 90")
        void nineMissGameScoresNinety() {
            for (int i = 0; i < 10; i++) {
                game.roll(9);
                game.roll(0);
            }
            assertEquals(90, game.score());
        }

        @Test
        @DisplayName("F-09 consecutive strikes build on each other (X X X then zeros)")
        void consecutiveStrikes() {
            rollAll(10, 10, 10);
            rollMany(14, 0);
            // F1: 30, F2: 20, F3: 10
            assertEquals(60, game.score());
        }
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Boundary limits (B-xx)")
    class Boundary {

        @ParameterizedTest(name = "B-01 pins = {0} is accepted")
        @ValueSource(ints = {0, 10})
        void extremePinCountsAreAccepted(int pins) {
            assertDoesNotThrow(() -> game.roll(pins));
        }

        @Test
        @DisplayName("B-02 10th-frame strike earns two fill balls")
        void tenthFrameStrikeGivesTwoFillBalls() {
            rollMany(18, 0);
            rollAll(10, 10, 10);
            assertTrue(game.isComplete());
            assertEquals(30, game.score());
        }

        @Test
        @DisplayName("B-03 10th-frame spare earns one fill ball")
        void tenthFrameSpareGivesOneFillBall() {
            rollMany(18, 0);
            rollAll(4, 6, 7);
            assertTrue(game.isComplete());
            assertEquals(17, game.score());
        }

        @Test
        @DisplayName("B-04 open 10th frame ends the game with no fill ball")
        void tenthFrameOpenEndsGameWithoutFillBall() {
            rollMany(18, 0);
            rollAll(4, 3);
            assertTrue(game.isComplete());
            assertEquals(7, game.score());
        }

        @Test
        @DisplayName("B-05 10th-frame strike followed by open fill balls")
        void tenthFrameStrikeThenOpenFillBalls() {
            rollMany(18, 0);
            rollAll(10, 3, 4);
            assertTrue(game.isComplete());
            assertEquals(17, game.score());
        }

        @Test
        @DisplayName("B-06 game is not complete after nine frames")
        void gameNotCompleteAfterNineFrames() {
            rollMany(18, 1);
            assertFalse(game.isComplete());
        }

        @Test
        @DisplayName("B-07 game is not complete after the 10th-frame spare, before the fill ball")
        void notCompleteBeforeFillBall() {
            rollMany(18, 0);
            rollAll(5, 5);
            assertFalse(game.isComplete());
        }

        @Test
        @DisplayName("B-08 10th-frame strike then strike then fill ball of 10 is allowed")
        void tenthFrameStrikeStrikeThenTenAllowed() {
            rollMany(18, 0);
            rollAll(10, 10);
            assertDoesNotThrow(() -> game.roll(10));
        }

        @Test
        @DisplayName("B-09 10th-frame strike allows a first fill ball of 0 then a spare-completing second ball")
        void tenthFrameStrikeThenZeroThenTen() {
            rollMany(18, 0);
            rollAll(10, 0);
            assertDoesNotThrow(() -> game.roll(10));
            assertEquals(20, game.score());
        }
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Edge and error conditions (E-xx)")
    class ErrorConditions {

        @ParameterizedTest(name = "E-01 pins = {0} is rejected")
        @ValueSource(ints = {-1, 11, 100, Integer.MIN_VALUE, Integer.MAX_VALUE})
        void invalidPinCountsRejected(int pins) {
            assertThrows(IllegalArgumentException.class, () -> game.roll(pins));
        }

        @Test
        @DisplayName("E-02 frame total over 10 is rejected")
        void frameTotalOverTenRejected() {
            game.roll(6);
            assertThrows(IllegalArgumentException.class, () -> game.roll(5));
        }

        @Test
        @DisplayName("E-03 10th-frame second roll making total over 10 is rejected")
        void tenthFrameSecondRollOverTenRejected() {
            rollMany(18, 0);
            game.roll(6);
            assertThrows(IllegalArgumentException.class, () -> game.roll(5));
        }

        @Test
        @DisplayName("E-04 10th-frame fill balls over 10 are rejected")
        void tenthFrameFillBallsOverTenRejected() {
            rollMany(18, 0);
            rollAll(10, 5);
            assertThrows(IllegalArgumentException.class, () -> game.roll(6));
        }

        @Test
        @DisplayName("E-05 rolling after the game is complete is rejected")
        void rollAfterGameCompleteRejected() {
            rollMany(20, 0);
            assertThrows(IllegalStateException.class, () -> game.roll(1));
        }

        @Test
        @DisplayName("E-06 scoring before the game is complete is rejected")
        void scoreBeforeCompletionRejected() {
            rollMany(10, 1);
            assertThrows(IllegalStateException.class, () -> game.score());
        }

        @Test
        @DisplayName("E-07 scoring a brand-new game is rejected")
        void scoreOnNewGameRejected() {
            assertThrows(IllegalStateException.class, () -> game.score());
        }

        @Test
        @DisplayName("E-08 a rejected roll does not change the game state")
        void rejectedRollLeavesStateUnchanged() {
            game.roll(6);
            assertThrows(IllegalArgumentException.class, () -> game.roll(5));
            assertDoesNotThrow(() -> game.roll(4)); // still waiting for the 2nd roll of frame 1
        }
    }
}
