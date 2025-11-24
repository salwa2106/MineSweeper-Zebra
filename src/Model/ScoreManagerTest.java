// src/Model/ScoreManagerTest.java
package Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ScoreManager – verifies scoring & life rules.
 */
public class ScoreManagerTest {

    private ScoreManager scoreManager;

    @BeforeEach
    void setUp() {
        // use any difficulty you want to test
        scoreManager = new ScoreManager(Difficulty.EASY);
    }

    /**
     * Small concrete Cell implementation for testing.
     */
    private static class TestCell extends Cell {
        TestCell(int row, int col, CellType type) {
            super(row, col, type);
        }
    }

    /** Helper: create a cell with given type and flagged state. */
    private Cell makeCell(CellType type, boolean flagged) {
        TestCell cell = new TestCell(0, 0, type);
        if (flagged) {
            cell.toggleFlag(); // now isFlagged() == true
        }
        return cell;
    }

    @Test
    void testInitialScoreIsZero() {
        assertEquals(0, scoreManager.getScore(), "Initial score should be 0");
    }

    @Test
    void testInitialLivesMatchDifficulty() {
        assertEquals(Difficulty.EASY.startLives, scoreManager.getLives(),
                "Lives should start according to difficulty.startLives");
    }

    @Test
    void testRevealEmptyAddsPoint() {
        Cell empty = makeCell(CellType.EMPTY, false);
        scoreManager.onReveal(empty);
        assertEquals(1, scoreManager.getScore(),
                "Revealing EMPTY should add +1 point");
        assertEquals(Difficulty.EASY.startLives, scoreManager.getLives(),
                "Revealing EMPTY should not change lives");
    }

    @Test
    void testRevealNumberAddsPoint() {
        Cell numberCell = makeCell(CellType.NUMBER, false);
        scoreManager.onReveal(numberCell);
        assertEquals(1, scoreManager.getScore(),
                "Revealing NUMBER should add +1 point");
    }

    @Test
    void testRevealQuestionAddsPoint() {
        Cell q = makeCell(CellType.QUESTION, false);
        scoreManager.onReveal(q);
        assertEquals(1, scoreManager.getScore(),
                "Revealing QUESTION should add +1 point (Iteration 1 rule)");
    }

    @Test
    void testRevealSurpriseAddsPoint() {
        Cell s = makeCell(CellType.SURPRISE, false);
        scoreManager.onReveal(s);
        assertEquals(1, scoreManager.getScore(),
                "Revealing SURPRISE should add +1 point (Iteration 1 rule)");
    }

    @Test
    void testRevealMineReducesLife() {
        int beforeLives = scoreManager.getLives();
        Cell mine = makeCell(CellType.MINE, false);

        scoreManager.onReveal(mine);

        assertEquals(beforeLives - 1, scoreManager.getLives(),
                "Revealing MINE should reduce lives by 1");
        assertEquals(0, scoreManager.getScore(),
                "Revealing MINE should not change score in Iteration 1");
    }

    @Test
    void testFlagMineGivesPoint() {
        Cell mine = makeCell(CellType.MINE, true); // already flagged

        scoreManager.onToggleFlag(mine);

        assertEquals(1, scoreManager.getScore(),
                "Flagging a MINE should give +1 point");
    }

    @Test
    void testFlagWrongCellLosesThreePoints() {
        Cell empty = makeCell(CellType.EMPTY, true); // flagged but not a mine

        scoreManager.onToggleFlag(empty);

        assertEquals(-3, scoreManager.getScore(),
                "Flagging a non-mine should give -3 points");
    }

    @Test
    void testGameOverWhenLivesReachZero() {
        Cell mine = makeCell(CellType.MINE, false);

        while (!scoreManager.isGameOver()) {
            scoreManager.onReveal(mine);
        }

        assertTrue(scoreManager.isGameOver(), "Game should be over when lives reach 0");
        assertEquals(0, scoreManager.getLives(), "Lives should not go below 0");
    }
}
