package com.wildhabitat.cli;

import com.wildhabitat.engine.GameEngine;
import com.wildhabitat.exception.InvalidPlacementException;
import com.wildhabitat.io.GridLoader;
import com.wildhabitat.io.SaveManager;
import com.wildhabitat.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Fully playable terminal version — completely independent of JavaFX.
 * No JavaFX import anywhere in this file.
 *
 * Compile (flat build):  javac -d out/cli src/cli/*.java src/core/*.java
 * Run (flat build):      java -cp out/cli CLIGame
 * Run (Maven shaded jar): java -jar target/wildhabitat.jar
 */

public class CLIGame {

    private final GameEngine engine;
    private final GameState  state;
    // Rolling history shown by the 'log' command
    private final List<String> history = new ArrayList<>();

    public CLIGame() {
        state  = new GameState();
        engine = new GameEngine(state);
    }

    public void run() {
        System.out.println("WildHabitat CLI");
        System.out.println("Loading grid...");

        try {
            GridLoader.loadDefault(state);
        } 
        catch (IOException e) {
            System.out.println("Warning: could not load gridstate.txt — using blank terrain.");
            fillBlankTerrain();
        }

        // Try loading a saved game, otherwise start fresh
        try {
            SaveManager.loadDefault(state);
            System.out.println("Save file loaded.");
        } 
        catch (IOException e) {
            System.out.println("No save file found — starting new game (Wave 1).");
            engine.triggerNextWave();
            appendHistory(state.messageLog);
        }

        System.out.println();

        printGrid();
        printHelp();

        Scanner scanner = new Scanner(System.in);

        while (!engine.isGameOver()) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            } 

            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            switch (cmd) {
                case "quit":
                case "exit":
                    System.out.println("Goodbye!");
                    return;

                case "save":
                    trySave();
                    break;

                case "load":
                    tryLoad();
                    break;

                case "next":
                    doNextTurn();
                    break;

                case "wave":
                    doWave();
                    break;

                case "place":
                    doPlace(parts);
                    break;

                case "log":
                    printHistory(20);
                    break;

                case "defenders":
                    printDefenderList();
                    break;

                case "help":
                    printHelp();
                    break;

                default:
                    System.out.println("Unknown command. Type 'help' for available commands.");
            }
        }

        //  Game over
        System.out.println();
        
        if (engine.didPlayerWin()) {
            System.out.println("  *** YOU WIN! All waves cleared! ***");
        } 
        else {
            System.out.println("  *** GAME OVER — The habitat was breached! ***");
        }

        System.out.println("  Final Score: " + state.score);
        
        scanner.close();
    }

    //  Commands

    private void doNextTurn() {
        List<String> msgs = engine.nextTurn();
        
        appendHistory(msgs);
        printGrid();

        for (String m : msgs) {
            System.out.println("  " + m);
        } 

        System.out.println();
    }

    private void doWave() {
        if (!state.livingAttackers().isEmpty()) {
            System.out.println("A wave is already in progress.");
            return;
        }

        engine.triggerNextWave();
        appendHistory(state.messageLog);
        
        for (String m : state.messageLog) {
            System.out.println("  " + m);
        } 

        System.out.println();
    }

    private void doPlace(String[] parts) {
        if (parts.length < 4) {
            System.out.println("Usage: place <type> <row> <col>");
            printDefenderList();
            return;
        }

        try {
            CreatureType type = CreatureType.fromName(parts[1]);
            int row = Integer.parseInt(parts[2]);
            int col = Integer.parseInt(parts[3]);

            // placeDefender throws InvalidPlacementException on illegal moves
            String result = engine.placeDefender(type, row, col);
            System.out.println(result);
            history.add(result);
        }
        catch (InvalidPlacementException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
        catch (NumberFormatException e) {
            System.out.println("Invalid row/col — must be integers.");
        }
        catch (IllegalArgumentException e) {
            System.out.println("Unknown type: " + parts[1]);
            printDefenderList();
        }
    }

    private void trySave() {
        // snapshotCreatures() uses Creature.clone() to capture a point-in-time copy
        List<Creature> snapshot = state.snapshotCreatures();
        try {
            SaveManager.saveDefault(state);
            System.out.println("Game saved to savegame.txt (" + snapshot.size() + " creatures).");
        }
        catch (IOException e) {
            System.out.println("Save failed: " + e.getMessage());
        }
    }

    private void tryLoad() {
        try {
            GridLoader.loadDefault(state);
            SaveManager.loadDefault(state);

            System.out.println("Game loaded from savegame.txt");

            printGrid();
        } 
        catch (IOException e) {
            System.out.println("Load failed: " + e.getMessage());
        }
    }

    //  Display 

    private void printGrid() {
        // Header — matches spec format exactly
        System.out.printf("WildHabitat CLI | Turn %d | Energy: %d | Score: %d | Wave: %d | Phase: %s %n",
                state.turn, state.energy, state.score, state.wave, state.timeOfDay.shortName());
        
        System.out.println(state.timeOfDay.phaseNote());
        
        System.out.println();

        // Column index row
        System.out.print("    ");

        for (int c = 0; c < GameState.COLS; ++c) {
            System.out.printf(" %2d ", c);
        }
        
        System.out.println();

        for (int r = 0; r < GameState.ROWS; ++r) {
            System.out.printf(" %d |", r);
           
            for (int c = 0; c < GameState.COLS; ++c) {
                Creature creature = state.getCreatureAt(r, c);

                String code = (creature != null) ? creature.type.abbrev : state.terrain[r][c].toCode();

                System.out.printf(" %s |", code);
            }

            System.out.println();
        }

        System.out.println();
    }

    private void printHistory(int count) {
        int start = Math.max(0, history.size() - count);
        System.out.println("--- Recent log (" + (history.size() - start) + " entries) ---");

        for (int i = start; i < history.size(); ++i) {
            System.out.println("  " + history.get(i));
        }

        System.out.println();
    }

    private void printDefenderList() {
        System.out.println("Available defenders:");

        for (CreatureType t : CreatureType.defenders()) {
            System.out.printf("  %-12s (%s)  cost %-3d  range %d%n",
                    t.displayName, t.abbrev, t.energyCost, t.attackRange);
        }
    }

    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("  place <type> <row> <col>  — place a defender");
        System.out.println("  next                      — advance one turn");
        System.out.println("  wave                      — start next attacker wave");
        System.out.println("  save                      — save game to savegame.txt");
        System.out.println("  load                      — load game from savegame.txt");
        System.out.println("  log                       — print recent event log");
        System.out.println("  defenders                 — list defender types and costs");
        System.out.println("  quit                      — exit");
    }

    // Helpers 

    private void appendHistory(List<String> msgs) {
        history.addAll(msgs);

        if (history.size() > 200) {
            history.subList(0, history.size() - 200).clear();
        } 
    }

    private void fillBlankTerrain() {
        for (int r = 0; r < GameState.ROWS; ++r) {
            for (int c = 0; c < GameState.COLS; ++c) {
                state.terrain[r][c] = Terrain.GRASS;
            }
        }
    }

    // Called by Main — kept as a static factory so CLIGame isn't instantiated by
    // the JavaFX launcher at class-load time and avoids accidental import conflicts.
    
    public static void start() {
        new CLIGame().run();
    }
}
