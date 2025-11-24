// src/Model/Cell.java
package Model;

public abstract class Cell {
    protected final int row;
    protected final int col;
    protected final CellType type;

    private boolean revealed = false;
    private boolean flagged = false;
    private int adjacentMines = 0; // used for NUMBER cells

    protected Cell(int row, int col, CellType type) {
        this.row = row;
        this.col = col;
        this.type = type;
    }

    public int getRow()           { return row; }
    public int getCol()           { return col; }
    public CellType getType()     { return type; }

    public boolean isRevealed()   { return revealed; }
    public boolean isFlagged()    { return flagged; }
    public int getAdjacentMines() { return adjacentMines; }

    public void reveal()          { this.revealed = true; }

    /** Toggle flag state (for controller). */
    public void toggleFlag()      { this.flagged = !this.flagged; }

    /** Package-private: used only by Board when calculating numbers. */
    void setAdjacentMines(int count) {
        this.adjacentMines = count;
    }
}
