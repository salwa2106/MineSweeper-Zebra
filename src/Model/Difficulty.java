package Model;

public enum Difficulty {
    EASY(9, 9, 10, 6, 2, 6, 10, 5),
    MEDIUM(13, 13, 26, 10, 3, 8, 10, 3),
    HARD(16, 16, 44, 14, 4, 10, 10, 2);

    public final int rows;
    public final int cols;
    public final int mines;
    public final int questionCells;
    public final int surpriseCells;
    public final int startLives;
    public final int maxLives;
    public final int questionOrSurpriseCost;

    Difficulty(int rows, int cols,
               int mines, int questionCells, int surpriseCells,
               int startLives, int maxLives, int questionOrSurpriseCost) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.questionCells = questionCells;
        this.surpriseCells = surpriseCells;
        this.startLives = startLives;
        this.maxLives = maxLives;
        this.questionOrSurpriseCost = questionOrSurpriseCost;
    }
}
