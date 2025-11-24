package Model;

public class NumberCell extends Cell {
    public NumberCell(int row, int col, int adjacentMines) {
        super(row, col, CellType.NUMBER);
        setAdjacentMines(adjacentMines);
    }
}