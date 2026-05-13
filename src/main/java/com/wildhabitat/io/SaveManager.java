package com.wildhabitat.io;

import com.wildhabitat.exception.SaveFileException;
import com.wildhabitat.model.*;

import java.io.*;

/**
 * Reads and writes savegame.txt.
 * Throws SaveFileException (a checked custom exception) when a CREATURE entry is malformed.
 * Uses Creature.create() factory to reconstruct the correct concrete subtype.
 */
public class SaveManager {

    public static void save(GameState state, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePath), java.nio.charset.StandardCharsets.UTF_8))) {
            writer.write("SCORE," + state.score);          writer.newLine();
            writer.write("TURN,"  + state.turn);           writer.newLine();
            writer.write("WAVE,"  + state.wave);           writer.newLine();
            writer.write("ENERGY," + state.energy);        writer.newLine();
            writer.write("TIME_OF_DAY," + state.timeOfDay.name()); writer.newLine();
            writer.write("DAY_CYCLE_TICK," + state.dayCycleTick);  writer.newLine();

            for (Creature c : state.creatures) {
                if (c.isAlive()) {
                    writer.write("CREATURE," + c.type.displayName
                            + "," + c.row + "," + c.col + "," + c.health);
                    writer.newLine();
                }
            }
        }
    }

    public static void saveDefault(GameState state) throws IOException {
        save(state, "savegame.txt");
    }

    /**
     * Loads a save file back into state.
     * Terrain must already be loaded (call GridLoader first).
     *
     * @throws SaveFileException if a CREATURE line is missing required fields
     * @throws IOException       on any other I/O error
     */
    public static void load(GameState state, String filePath) throws IOException {
        state.creatures.clear();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filePath), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine && line.startsWith("﻿")) line = line.substring(1);
                firstLine = false;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",");

                switch (parts[0].toUpperCase()) {
                    case "SCORE":          state.score      = Integer.parseInt(parts[1]); break;
                    case "TURN":           state.turn       = Integer.parseInt(parts[1]); break;
                    case "WAVE":           state.wave       = Integer.parseInt(parts[1]); break;
                    case "ENERGY":         state.energy     = Integer.parseInt(parts[1]); break;
                    case "TIME_OF_DAY":    state.timeOfDay  = TimeOfDay.fromString(parts[1]); break;
                    case "DAY_CYCLE_TICK": state.dayCycleTick = Integer.parseInt(parts[1]); break;

                    case "CREATURE": {
                        // Expected format: CREATURE,<type>,<row>,<col>,<health>
                        if (parts.length < 5) {
                            throw new SaveFileException(
                                    "Malformed CREATURE entry (expected 5 fields): " + line);
                        }
                        CreatureType type   = CreatureType.fromName(parts[1]);
                        int          row    = Integer.parseInt(parts[2]);
                        int          col    = Integer.parseInt(parts[3]);
                        int          health = Integer.parseInt(parts[4]);
                        // Creature.create() returns AttackerCreature or DefenderCreature
                        state.creatures.add(Creature.create(type, row, col, health));
                        break;
                    }

                    default:
                        System.out.println("[SaveManager] Unknown key ignored: " + parts[0]);
                }
            }
        }
    }

    public static void loadDefault(GameState state) throws IOException {
        load(state, "savegame.txt");
    }
}
