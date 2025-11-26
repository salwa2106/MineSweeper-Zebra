package Model;

/**
 * Implements scoring and life rules for the game.
 *
 * Iteration 1:
 *  - Reveal mine → lose life
 *  - Reveal EMPTY/NUMBER/QUESTION/SURPRISE → +1 point
 *  - Flag mine → +1 point
 *  - Flag non-mine → -3 points
 *
 * Iteration 2 (added):
 *  - Handle question outcome (correct / wrong) with points & lives
 *  - Handle surprise outcome with points & lives
 *  - Gain lives with overflow conversion to points
 */
public class ScoreManager {

    private int score = 0;
    private int lives;

    private final int maxLives;
    private final Difficulty difficulty;

    public ScoreManager(Difficulty difficulty) {
        if (difficulty == null) {
            difficulty = Difficulty.EASY;
        }
        this.difficulty = difficulty;
        this.lives = difficulty.startLives;
        this.maxLives = difficulty.maxLives;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public int getMaxLives() {
        return maxLives;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public boolean isGameOver() {
        return lives <= 0;
    }

    /* ----------------------------------------------------------
     *  ITERATION 1 BEHAVIOUR (UNCHANGED)
     * ---------------------------------------------------------- */

    /** Called when a cell is revealed on the board. */
    public void onReveal(Cell cell) {
        switch (cell.getType()) {
            case MINE -> {
                // Revealing a mine: lose 1 life (no score change)
                loseLife(1);
            }
            case NUMBER, EMPTY, QUESTION, SURPRISE -> {
                // Basic reveal of a non-mine cell = +1 point
                score += 1;
            }
            default -> {
                // no-op
            }
        }
    }

    /**
     * Called when player toggles a flag on a cell.
     * We check the state *after* the toggle using cell.isFlagged().
     */
    public void onToggleFlag(Cell cell) {
        // If the cell is flagged *now*:
        //   - If it's a mine → +1 point
        //   - If it's not a mine → -3 points
        // If unflagged → no change (Iteration 1 rule)
        if (cell.isFlagged()) {
            if (cell.getType() == CellType.MINE) {
                score += 1;
            } else {
                score -= 3;
            }
        }
    }

    /* ----------------------------------------------------------
     *  ITERATION 2 – QUESTIONS
     * ---------------------------------------------------------- */

    /**
     * Apply the result of a trivia question to score & lives.
     *
     * The caller (controller/GUI) passes the Question and whether
     * the player answered correctly.
     *
     * Points & life changes come from the Question itself; if some
     * fields are null, we fall back to Difficulty / default values.
     */
    public void applyQuestionOutcome(Question question, boolean correct) {
        if (question == null) {
            // Fallback: simple +/- using difficulty.questionOrSurpriseCost
            int base = difficulty.questionOrSurpriseCost;
            if (correct) {
                addPoints(base);
            } else {
                addPoints(-base);
                loseLife(1);
            }
            return;
        }

        // Use question-specific scoring if present
        int lifeDelta = (question.getLifeDelta() != null)
                ? question.getLifeDelta()
                : 1;

        int pointsRight = (question.getPointsRight() != null)
                ? question.getPointsRight()
                : difficulty.questionOrSurpriseCost;

        int pointsWrong = (question.getPointsWrong() != null)
                ? question.getPointsWrong()
                : -difficulty.questionOrSurpriseCost;

        if (correct) {
            addPoints(pointsRight);
            gainLife(lifeDelta);
        } else {
            addPoints(pointsWrong);
            loseLife(lifeDelta);
        }
    }

    /* ----------------------------------------------------------
     *  ITERATION 2 – SURPRISE CELLS
     * ---------------------------------------------------------- */

    /**
     * Apply the effect of a surprise cell.
     *
     * pointsDelta  – points gained (positive) or lost (negative)
     * lifeDelta    – lives gained (positive) or lost (negative)
     */
    public void applySurpriseOutcome(int pointsDelta, int lifeDelta) {
        if (pointsDelta != 0) {
            addPoints(pointsDelta);
        }
        if (lifeDelta > 0) {
            gainLife(lifeDelta);
        } else if (lifeDelta < 0) {
            loseLife(-lifeDelta);
        }
    }

    /* ----------------------------------------------------------
     *  LOW-LEVEL HELPERS (LIVES & SCORE)
     * ---------------------------------------------------------- */

    /** Add points (can be negative). */
    public void addPoints(int delta) {
        this.score += delta;
    }

    /** Lose up to n lives; does not go below zero. */
    public void loseLife(int n) {
        if (n <= 0) return;
        lives = Math.max(0, lives - n);
    }

    /**
     * Gain up to n lives; if we exceed maxLives,
     * extra lives are converted into score using
     * difficulty.questionOrSurpriseCost as the
     * "value" of an extra life.
     */
    public void gainLife(int n) {
        if (n <= 0) return;

        int before = lives;
        lives = Math.min(maxLives, lives + n);

        int overflow = before + n - maxLives;
        if (overflow > 0) {
            // convert extra lives to points
            int perLife = difficulty.questionOrSurpriseCost;
            addPoints(overflow * perLife);
        }
    }
}
