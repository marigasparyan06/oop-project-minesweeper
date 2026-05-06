package java.board;

import java.enums.TileType;

public class Board {
    public static final int ROWS = 7;
    public static final int COLS = 11;

    private final Cell[][] grid;

    public Board() {
        grid = new Cell[ROWS][COLS];
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                grid[r][c] = new Cell(r, c, TileType.GRASS);
    }

}
