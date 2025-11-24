// src/Model/ScoreManager.java
package Model;

/**
 * Implements scoring rules for revealing / flagging basic cells according
 * to the spec: mines, numbers, empty cells. Question/Surprise activation
 * will be handled in later iterations.
 */
public class ScoreManager {

    private int score = 0;
    private int lives;
    private final int maxLives = 10;
    private final Difficulty difficulty;

    public ScoreManager(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.lives = difficulty.startLives;
    }

    public int getScore() { return score; }
    public int getLives() { return lives; }

    public boolean isGameOver() {
        return lives <= 0;
    }

    /** Called when a cell is revealed. */
    public void onReveal(Cell cell) {
        switch (cell.getType()) {
            case MINE -> {
                // "חשיפת המשבצת מפסידה חיים (💔-1)" :contentReference[oaicite:5]{index=5}
                loseLife(1);
            }
            case NUMBER, EMPTY, QUESTION, SURPRISE -> {
                // reveal = +1 point for non-mine basic reveal :contentReference[oaicite:6]{index=6}
                score += 1;
            }
            default -> {}
        }
    }

    /**
     * Called when player toggles flag (we don't know yet if it's
     * correct or not, only the action itself).
     */
    public void onToggleFlag(Cell cell) {
        // from spec: flagging EMPTY/NUMBER/QUESTION/SURPRISE = -3 points,
        // flagging a MINE = +1 point. :contentReference[oaicite:7]{index=7}
        if (cell.isFlagged()) {
            if (cell.getType() == CellType.MINE) {
                score += 1;
            } else {
                score -= 3;
            }
        } else {
            // unflagging – for Iteration 1 we can leave as "no change"
        }
    }

    private void loseLife(int n) {
        lives = Math.max(0, lives - n);
    }
}
