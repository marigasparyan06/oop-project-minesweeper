package main.java.wildhabitat;

import java.util.*;

/**
 * Manages attacker waves. Each wave spawns attackers on the left column.
 * Wave difficulty scales with wave number and current phase.
 */
public class WaveManager {
    private int currentWave;
    private boolean waveActive;
    private Random rng;

    // Attacker types available to spawn
    private static final Creature.Type[] ATTACKER_POOL = {
        Creature.Type.Wolf, Creature.Type.Rabbit, Creature.Type.Boar,
        Creature.Type.NightStalker, Creature.Type.SwampCrawler
    };

    public WaveManager() {
        this.currentWave = 1;
        this.waveActive = false;
        this.rng = new Random();
    }

    public WaveManager(int wave) {
        this.currentWave = wave;
        this.waveActive = false;
        this.rng = new Random();
    }

    public int getCurrentWave() { return currentWave; }
    public boolean isWaveActive() { return waveActive; }
    public void setWaveActive(boolean v) { waveActive = v; }

    /**
     * Generate a list of attackers for the current wave.
     * Spawns them on column 0, spread across random rows.
     */
    public List<Creature> spawnWave(DayNightCycle.Phase phase) {
        List<Creature> spawned = new ArrayList<>();
        int count = 2 + currentWave;  // wave 1 = 3 attackers, wave 2 = 4, etc.

        // Night spawns extra NightStalkers
        boolean isNight = (phase == DayNightCycle.Phase.NIGHT);

        Set<Integer> usedRows = new HashSet<>();
        for (int i = 0; i < count; i++) {
            int row;
            int attempts = 0;

            do {
                row = rng.nextInt(Grid.ROWS);
                attempts++;
            
            } while (usedRows.contains(row) && attempts < 20);
            
            usedRows.add(row);

            Creature.Type type;
            if (isNight && rng.nextInt(3) == 0) {
                type = Creature.Type.NightStalker;
            } 
            else if (phase == DayNightCycle.Phase.DUSK && rng.nextBoolean()) {
                type = Creature.Type.SwampCrawler;
            } 
            else {
                type = ATTACKER_POOL[rng.nextInt(ATTACKER_POOL.length)];
            }
            spawned.add(new Creature(type, row, 0));
        }
        
        waveActive = true;
        ++currentWave;
        return spawned;
    }

    /** Check if all attackers from the wave are dead — wave complete. */
    public boolean isWaveComplete(List<Creature> creatures) {
        for (Creature c : creatures) {
            if (c.getRole() == Creature.Role.ATTACKER && c.isAlive()) {
                return false;
            }
        }
        
        return true;
    }
}

