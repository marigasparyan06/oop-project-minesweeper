package com.wildhabitat.io;

import com.wildhabitat.model.GameState;
import com.wildhabitat.model.Terrain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/** Reads gridstate.txt and populates the terrain array in a GameState. */
public class GridLoader {

    /**
     * Loads terrain from the given file path into state.terrain.
     * Throws IOException on read failure; unchecked IllegalArgumentException
     * if a cell token is unrecognised.
     */
    public static void load(GameState state, String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath, java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            int row = 0;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null && row < GameState.ROWS) {
                
                if (firstLine && line.startsWith("﻿")) {
                    line = line.substring(1);
                }

                firstLine = false;
                line = line.trim();
                
                if (line.isEmpty()) {
                    continue;
                } 
                
                String[] tokens = line.split(",");
                
                for (int col = 0; col < Math.min(tokens.length, GameState.COLS); col++) {
                    state.terrain[row][col] = Terrain.fromString(tokens[col].trim());
                }
                
                ++row;
            }
        }
    }

    /** Convenience: load from the default gridstate.txt in the working directory. */
    public static void loadDefault(GameState state) throws IOException {
        load(state, "gridstate.txt");
    }
}
