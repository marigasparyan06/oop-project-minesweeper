package wildhabitat.exceptions;

public class InvalidTileException extends Exception {
    public InvalidTileException(int row, int col) {
        super("Out-of-bounds tile access at row=" + row + ", col=" + col);
    }
}
