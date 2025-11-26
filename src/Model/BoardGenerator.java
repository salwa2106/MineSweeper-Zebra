package Model;

/**
 * Factory-style class for creating Boards for the given difficulty.
 * Generates a full board with mines, question cells, surprise cells
 * and computes adjacency numbers according to the Difficulty config.
 */
public class BoardGenerator {

    public Board generate(Difficulty difficulty) {
        return new Board(difficulty);
    }
}
