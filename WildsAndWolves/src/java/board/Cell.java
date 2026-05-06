package java.board;

import java.enums.TileType;

public class Cell {
    private final int row;
    private final int col;
    private final TileType tileType;

    public Cell(int row, int col, TileType tileType) {
        this.row = row;
        this.col = col;
        this.tileType = tileType;
    }

}
