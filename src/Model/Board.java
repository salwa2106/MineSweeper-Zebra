package Model;

import java.util.*;

/**
 * Logical representation of a single Minesweeper board for one player.
 * Handles random generation and cascade reveal. No GUI here.
 */
public class Board {

    private final Difficulty difficulty;
    private final int rows;
    private final int cols;

    private final Cell[][] grid;
    private final Random random;

    public Board(Difficulty diff) {
        this(diff, new Random());
    }

    public Board(Difficulty diff, Random random) {
        // ------ IMPORTANT: NEVER allow null difficulty ------
        if (diff == null) {
            diff = Difficulty.EASY; // safe default so we don't crash
        }
        this.difficulty = diff;
        this.rows = diff.rows;
        this.cols = diff.cols;
        this.random = (random != null) ? random : new Random();
        this.grid = new Cell[rows][cols];

        generate();
    }

    public Difficulty getDifficulty() { return difficulty; }
    public int getRows()              { return rows; }
    public int getCols()              { return cols; }

    public Cell getCell(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) {
            throw new IndexOutOfBoundsException("Cell out of board: " + r + "," + c);
        }
        return grid[r][c];
    }

    // ---------- Generation logic: mines, numbers, specials ----------

    private void generate() {
        // start EMPTY
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new EmptyCell(r, c);
            }
        }

        // 1) place mines
        placeRandomCells(CellType.MINE, difficulty.mines);

        // 2) compute numbers
        calculateNumbers();

        // 3) place questions on empty cells
        placeRandomCells(CellType.QUESTION, difficulty.questionCells);

        // 4) place surprises on empty cells
        placeRandomCells(CellType.SURPRISE, difficulty.surpriseCells);
    }

    private void placeRandomCells(CellType type, int count) {
        int placed = 0;
        while (placed < count) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            Cell current = grid[r][c];
            if (current.getType() != CellType.EMPTY) continue;

            switch (type) {
                case MINE      -> grid[r][c] = new MineCell(r, c);
                case QUESTION  -> grid[r][c] = new QuestionCell(r, c);
                case SURPRISE  -> grid[r][c] = new SurpriseCell(r, c);
                default -> throw new IllegalArgumentException("Unsupported placement type: " + type);
            }
            placed++;
        }
    }

    private void calculateNumbers() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c].getType() == CellType.MINE) continue;

                int minesAround = countAdjacentMines(r, c);
                if (minesAround > 0) {
                    grid[r][c] = new NumberCell(r, c, minesAround);
                } else {
                    ((Cell) grid[r][c]).setAdjacentMines(0);
                }
            }
        }
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = row + dr, nc = col + dc;
                if (isInside(nr, nc) && grid[nr][nc].getType() == CellType.MINE) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isInside(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    /**
     * Reveal a region starting from (row,col). Empty cells cause cascade.
     * Returns the list of all cells that became revealed now.
     */
    public List<Cell> revealCascade(int row, int col) {
        List<Cell> revealed = new ArrayList<>();
        Cell start = getCell(row, col);
        if (start.isRevealed() || start.isFlagged()) return revealed;

        Queue<Cell> queue = new ArrayDeque<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            Cell cell = queue.remove();
            if (cell.isRevealed() || cell.isFlagged()) continue;

            cell.reveal();
            revealed.add(cell);

            if (cell.getType() == CellType.EMPTY) {
                int r = cell.getRow();
                int c = cell.getCol();
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        int nr = r + dr, nc = c + dc;
                        if (isInside(nr, nc)) {
                            Cell neighbor = grid[nr][nc];
                            if (!neighbor.isRevealed() && !neighbor.isFlagged()) {
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }
        }

        return revealed;
    }


    public boolean isAllSafeCellsRevealed() {
        // Go over all cells in the board
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = getCell(r, c);

                // "Safe" = any cell that is NOT a mine
                if (cell.getType() != CellType.MINE && !cell.isRevealed()) {
                    // There is at least one safe cell still hidden → not yet won
                    return false;
                }
            }
           
        }
        // All non-mine cells are revealed → board is cleared
        return true;
    }

}
