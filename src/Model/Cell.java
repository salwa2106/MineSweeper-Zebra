// src/Model/Cell.java
package Model;

public abstract class Cell {
    protected final int row;
    protected final int col;
    protected final CellType type;

    private boolean flagScored = false;      // score awarded for flagging (first time only)
    private boolean revealScored = false;    // score awarded for revealing (first time only)

    private boolean revealed = false;
    private boolean flagged  = false;
    private int adjacentMines = 0; // used for NUMBER cells

    // 🔹 NEW: used for Question / Surprise second-step activation
    private boolean specialUsed = false;

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

    /** Used only by Board during setup. */
    void setAdjacentMines(int count) {
        this.adjacentMines = count;
    }

    // 🔹 For Question / Surprise 2nd click
    public boolean isSpecialUsed() {
        return specialUsed;
    }

    public void setSpecialUsed(boolean used) {
        this.specialUsed = used;
    }

    // 🔹 Score control for flags
    public boolean isFlagScored() {
        return flagScored;
    }

    public void setFlagScored(boolean used) {
        this.flagScored = used;
    }

    // 🔹 Score control for reveals (NEW)
    public boolean isRevealScored() {
        return revealScored;
    }

    public void setRevealScored(boolean used) {
        this.revealScored = used;
    }
}
