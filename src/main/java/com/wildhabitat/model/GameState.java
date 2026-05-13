package com.wildhabitat.model;

import com.wildhabitat.util.EventLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete, serialisable game state.
 * Engine mutates this; UI / CLI read it for display.
 *
 * messageLog is an EventLog<String> — a generic bounded list (Generics).
 * snapshotCreatures() uses Creature.clone() to demonstrate Cloneable.
 */
public class GameState {

    public static final int ROWS = 7;
    public static final int COLS = 11;

    // Grid terrain (immutable after load)
    public Terrain[][] terrain = new Terrain[ROWS][COLS];

    // All living creatures (attackers + defenders mixed, filter by role as needed)
    public List<Creature> creatures = new ArrayList<>();

    public int score = 0;
    public int turn  = 1;
    public int wave  = 1;
    public int energy = 100;

    public TimeOfDay timeOfDay   = TimeOfDay.DAY;
    public int       dayCycleTick = 0;

    public static final int PHASE_DURATION = 3;

    /**
     * Running log of messages visible in the UI / CLI.
     * EventLog<String> automatically evicts old entries once the capacity is reached.
     */
    public EventLog<String> messageLog = new EventLog<>(200);

    // ── Logging ───────────────────────────────────────────────────────────────

    public void log(String msg) {
        messageLog.add(msg);
    }

    // ── Snapshot (Cloneable) ──────────────────────────────────────────────────

    /**
     * Returns a list of cloned copies of all living creatures.
     * Uses Creature.clone() — the snapshot is independent of the live state,
     * so it can safely be serialised or inspected after the turn advances.
     */
    public List<Creature> snapshotCreatures() {
        List<Creature> snapshot = new ArrayList<>(creatures.size());
        for (Creature c : creatures) {
            if (c.isAlive()) {
                snapshot.add(c.clone());
            }
        }
        return snapshot;
    }

    // ── Grid queries ──────────────────────────────────────────────────────────

    /** Returns the first living creature occupying (row, col), or null. */
    public Creature getCreatureAt(int row, int col) {
        for (Creature c : creatures) {
            if (c.isAlive() && c.row == row && c.col == col) {
                return c;
            }
        }
        return null;
    }

    /** Returns every living attacker occupying (row, col). */
    public List<Creature> getAttackersAt(int row, int col) {
        List<Creature> res = new ArrayList<>();
        for (Creature c : creatures) {
            if (c.isAlive() && c.isAttacker() && c.row == row && c.col == col) {
                res.add(c);
            }
        }
        return res;
    }

    /** True if the cell is empty (no living creature of any role). */
    public boolean isCellEmpty(int row, int col) {
        return getCreatureAt(row, col) == null;
    }

    /** True if the cell has a living defender. */
    public boolean hasDefender(int row, int col) {
        Creature c = getCreatureAt(row, col);
        return c != null && c.isDefender();
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    /** All living attackers. */
    public List<Creature> livingAttackers() {
        List<Creature> res = new ArrayList<>();
        for (Creature c : creatures) {
            if (c.isAlive() && c.isAttacker()) res.add(c);
        }
        return res;
    }

    /** All living defenders. */
    public List<Creature> livingDefenders() {
        List<Creature> res = new ArrayList<>();
        for (Creature c : creatures) {
            if (c.isAlive() && c.isDefender()) res.add(c);
        }
        return res;
    }

    /** Remove dead creatures from the list. */
    public void purgeDeadCreatures() {
        creatures.removeIf(c -> !c.isAlive());
    }

    /** Advance the day/night cycle by one tick; returns true if the phase just changed. */
    public boolean tickDayCycle() {
        ++dayCycleTick;
        if (dayCycleTick >= PHASE_DURATION) {
            dayCycleTick = 0;
            timeOfDay = timeOfDay.next();
            return true;
        }
        return false;
    }
}
