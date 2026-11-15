package com.example.bowling;

/** Scores a single game of ten-pin bowling with input validation. */
public class BowlingGame {

    private final int[] rolls = new int[21];
    private int rollCount = 0;

    private int frame = 1;          // current frame, 1..10
    private int rollInFrame = 1;    // 1, 2 (or 3 in frame 10)
    private int first = 0;          // first roll of current frame
    private int second = 0;         // second roll (frame 10 only)
    private boolean complete = false;

    /**
     * Records one roll.
     *
     * @param pins pins knocked down, 0..10
     * @throws IllegalArgumentException if pins is out of range or the frame total is impossible
     * @throws IllegalStateException    if the game is already complete
     */
    public void roll(int pins) {
        if (pins < 0 || pins > 10) {
            throw new IllegalArgumentException("Pins must be between 0 and 10: " + pins);
        }
        if (complete) {
            throw new IllegalStateException("Game is already complete");
        }

        if (frame < 10) {
            if (rollInFrame == 1) {
                rolls[rollCount++] = pins;
                if (pins == 10) {
                    frame++;                 // strike: frame ends
                } else {
                    first = pins;
                    rollInFrame = 2;
                }
            } else {
                if (first + pins > 10) {
                    throw new IllegalArgumentException("Frame total exceeds 10 pins");
                }
                rolls[rollCount++] = pins;
                frame++;
                rollInFrame = 1;
            }
            return;
        }

        // ---- 10th frame ----
        if (rollInFrame == 1) {
            rolls[rollCount++] = pins;
            first = pins;
            rollInFrame = 2;
        } else if (rollInFrame == 2) {
            if (first != 10 && first + pins > 10) {
                throw new IllegalArgumentException("Frame total exceeds 10 pins");
            }
            rolls[rollCount++] = pins;
            second = pins;
            if (first + second < 10) {
                complete = true;             // open frame, no fill ball
            } else {
                rollInFrame = 3;             // strike or spare earns a fill ball
            }
        } else {
            if (first == 10 && second != 10 && second + pins > 10) {
                throw new IllegalArgumentException("Fill balls exceed 10 pins");
            }
            rolls[rollCount++] = pins;
            complete = true;
        }
    }

    /** @return true once all ten frames (including fill balls) have been rolled. */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Computes the final score.
     *
     * @throws IllegalStateException if the game is not complete
     */
    public int score() {
        if (!complete) {
            throw new IllegalStateException("Game is not complete");
        }
        int score = 0;
        int i = 0;
        for (int f = 0; f < 10; f++) {
            if (rolls[i] == 10) {                          // strike
                score += 10 + rolls[i + 1] + rolls[i + 2];
                i += 1;
            } else if (rolls[i] + rolls[i + 1] == 10) {    // spare
                score += 10 + rolls[i + 2];
                i += 2;
            } else {                                       // open frame
                score += rolls[i] + rolls[i + 1];
                i += 2;
            }
        }
        return score;
    }
}
