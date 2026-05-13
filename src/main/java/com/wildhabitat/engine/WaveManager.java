package com.wildhabitat.engine;

import com.wildhabitat.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Decides which attackers spawn each wave and at what rows.
 * Attackers enter from col 0 (left edge) and march right toward col 10.
 * Terrain-compatible row assignment is handled by GameEngine.triggerNextWave().
 */
public class WaveManager {

    private static final Random rng = new Random(42);

    /**
     * Returns the list of creatures to spawn for the given wave number.
     * Rows are randomly assigned here; GameEngine reassigns them to terrain-compatible rows.
     */
    public static List<Creature> buildWave(int waveNumber) {
        List<Creature> spawns = new ArrayList<>();

        switch (waveNumber) {
            case 1:
                spawnGroup(spawns, CreatureType.WOLF, 3);
                break;
            case 2:
                spawnGroup(spawns, CreatureType.WOLF,   2);
                spawnGroup(spawns, CreatureType.RABBIT, 2);
                break;
            case 3:
                spawnGroup(spawns, CreatureType.WOLF,  4);
                spawnGroup(spawns, CreatureType.BOAR,  1);
                break;
            case 4:
                spawnGroup(spawns, CreatureType.WOLF,   3);
                spawnGroup(spawns, CreatureType.RABBIT, 2);
                spawnGroup(spawns, CreatureType.BOAR,   1);
                break;
            case 5:
                spawnGroup(spawns, CreatureType.WOLF,         4);
                spawnGroup(spawns, CreatureType.SWAMPCRAWLER, 2);
                spawnGroup(spawns, CreatureType.NIGHTSTALKER, 1);
                break;
            case 6:
                spawnGroup(spawns, CreatureType.WOLF,         3);
                spawnGroup(spawns, CreatureType.RABBIT,       3);
                spawnGroup(spawns, CreatureType.NIGHTSTALKER, 2);
                break;
            case 7:
                spawnGroup(spawns, CreatureType.BOAR,         3);
                spawnGroup(spawns, CreatureType.SWAMPCRAWLER, 3);
                spawnGroup(spawns, CreatureType.NIGHTSTALKER, 2);
                break;
            default: {
                int extra = (waveNumber - 8) / 2;
                spawnGroup(spawns, CreatureType.WOLF,         3 + extra);
                spawnGroup(spawns, CreatureType.RABBIT,       2 + extra);
                spawnGroup(spawns, CreatureType.BOAR,         1 + extra / 2);
                spawnGroup(spawns, CreatureType.NIGHTSTALKER, 1 + extra / 3);
                spawnGroup(spawns, CreatureType.SWAMPCRAWLER, 1 + extra / 2);
                break;
            }
        }

        return spawns;
    }

    private static void spawnGroup(List<Creature> out, CreatureType type, int count) {
        for (int i = 0; i < count; i++) {
            int row = rng.nextInt(GameState.ROWS);
            for (int attempt = 0; attempt < 10; attempt++) {
                boolean clash = false;
                for (Creature existing : out) {
                    if (existing.row == row) { clash = true; break; }
                }
                if (!clash) break;
                row = rng.nextInt(GameState.ROWS);
            }
            out.add(Creature.create(type, row, 0));
        }
    }

    /** Returns true if no living attackers remain on the grid. */
    public static boolean isWaveCleared(GameState state) {
        return state.livingAttackers().isEmpty();
    }
}
