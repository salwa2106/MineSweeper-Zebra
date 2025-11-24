package Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BoardGenerator – verifies mine distribution and adjacent-mine counts.
 */
public class BoardGeneratorTest {

    private BoardGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new BoardGenerator();
    }

    @Test
    void testGenerateEasyBoard_MineQuestionSurpriseCounts() {
        Difficulty diff = Difficulty.EASY;

        Board board = generator.generate(diff);
        assertNotNull(board, "Board should not be null");

        assertEquals(diff.rows, board.getRows(), "Row count should match difficulty");
        assertEquals(diff.cols, board.getCols(), "Column count should match difficulty");

        int mines = 0;
        int questions = 0;
        int surprises = 0;

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                assertNotNull(cell);

                switch (cell.getType()) {
                    case MINE -> mines++;
                    case QUESTION -> questions++;
                    case SURPRISE -> surprises++;
                    default -> {}
                }
            }
        }

        assertEquals(diff.mines, mines, "Mine count mismatch");
        assertEquals(diff.questionCells, questions, "Question count mismatch");
        assertEquals(diff.surpriseCells, surprises, "Surprise count mismatch");
    }

    @Test
    void testAdjacentMineCountsAreCorrect() {
        Difficulty diff = Difficulty.MEDIUM;
        Board board = generator.generate(diff);

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);

                if (cell.getType() == CellType.MINE) continue;

                int expected = countNeighborMines(board, r, c);
                assertEquals(expected, cell.getAdjacentMines(),
                        "Wrong adjacent mine count at [" + r + "," + c + "]");
            }
        }
    }

    private int countNeighborMines(Board board, int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;

                int rr = row + dr, cc = col + dc;
                if (rr < 0 || rr >= board.getRows() ||
                    cc < 0 || cc >= board.getCols()) continue;

                if (board.getCell(rr, cc).getType() == CellType.MINE) {
                    count++;
                }
            }
        }
        return count;
    }
}
