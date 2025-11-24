package Controller;

import Model.*;

import java.util.List;

public class GameController {

    private final Board[] boards = new Board[2];
    private final Player[] players = new Player[3];
    private final ScoreManager scoreManager;
    private int currentPlayerIdx = 0;   // 0 or 1

    public GameController(String p1Name, String p2Name, Difficulty difficulty) {
        if (difficulty == null) difficulty = Difficulty.EASY;

        players[0] = new Player(p1Name);
        players[1] = new Player(p2Name);

        boards[0] = new Board(difficulty);
        boards[1] = new Board(difficulty);

        scoreManager = new ScoreManager(difficulty);
    }

    public Board getBoard(int playerIdx) {
        return boards[playerIdx];
    }

    public Player getPlayer(int playerIdx) {
        return players[playerIdx];
    }

    public int getCurrentPlayerIdx() {
        return currentPlayerIdx;
    }

    public Player getCurrentPlayer() {
        return players[currentPlayerIdx];
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public boolean isGameOver() {
        return scoreManager.isGameOver();
    }

    /** Reveal a cell for the given player; returns all cells that became revealed (for cascade). */
    public List<Cell> revealCell(int playerIdx, int row, int col) {
        Board board = boards[playerIdx];
        Cell cell = board.getCell(row, col);
        if (cell.isRevealed() || cell.isFlagged()) return List.of();

        List<Cell> revealed;
        if (cell.getType() == CellType.EMPTY) {
            revealed = board.revealCascade(row, col);
        } else {
            cell.reveal();
            revealed = List.of(cell);
        }

        for (Cell c : revealed) {
            scoreManager.onReveal(c);
        }

        // after reveal, switch turn
        currentPlayerIdx = 1 - currentPlayerIdx;
        return revealed;
    }

    /** Toggle flag for the given player. */
    public void toggleFlag(int playerIdx, int row, int col) {
        Board board = boards[playerIdx];
        Cell cell = board.getCell(row, col);
        cell.toggleFlag();
        scoreManager.onToggleFlag(cell);
    }
}
