package Model;

/**
 * Factory-style class for creating Boards for the given difficulty.
 * Simple wrapper so tests and controllers can call generate().
 */
public class BoardGenerator {

    public Board generate(Difficulty difficulty) {
        return new Board(difficulty);
    }
}
