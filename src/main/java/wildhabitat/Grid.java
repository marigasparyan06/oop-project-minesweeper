package main.java.wildhabitat;

import java.io.*;
import java.util.*;

public class Grid {
    public static final int ROWS = 7;
    public static final int COLS = 11;

    public enum Terrain { GRASS, STONE, SHALLOW_WATER, DEEP_WATER }

    private Terrain[][] cells;

    public Grid() {
        cells = new Terrain[ROWS][COLS];

        for (Terrain[] row : cells) {
            Arrays.fill(row, Terrain.GRASS);
        }
    }

    public Terrain get(int row, int col) { 
        return cells[row][col]; 
    }

    public void set(int row, int col, Terrain t) { 
        cells[row][col] = t; 
    }

    /** Load grid from gridstate.txt. Each line = one row, comma-separated terrain names. */
    public void loadFromFile(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            int row = 0;
            String line;
            
            while ((line = br.readLine()) != null && row < ROWS) {
                String[] parts = line.trim().split(",");
            
                for (int col = 0; col < Math.min(parts.length, COLS); col++) {
                    try {
                        cells[row][col] = Terrain.valueOf(parts[col].trim());
                    } 
                    catch (IllegalArgumentException e) {
                        cells[row][col] = Terrain.GRASS;
                    }
                }
                row++;
            }
        }
    }

    /** Save grid to gridstate.txt. */
    public void saveToFile(String path) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {

            for (int r = 0; r < ROWS; r++) {
                StringBuilder sb = new StringBuilder();

                for (int c = 0; c < COLS; c++) {
                    if (c > 0) {
                        sb.append(',');
                    } 

                    sb.append(cells[r][c].name());
                }
                
                bw.write(sb.toString());
                bw.newLine();
            }
        }
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }
}

