package com.wildhabitat.model;

import com.wildhabitat.util.EventLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete game state. The engine mutates this; the UI and CLI read it for display.
 * messageLog is a generic bounded list (EventLog).
 * snapshotCreatures() uses Creature.clone() to capture a point-in-time copy.
 */
public class GameState {

    public static final int ROWS = 7;
    public static final int COLS = 11;

    public Terrain[][] terrain = new Terrain[ROWS][COLS];

    public List<Creature> creatures = new ArrayList<>();

    public int score = 0;
    public int turn = 1;
    public int wave = 1;
    public int energy = 100;

    public TimeOfDay timeOfDay = TimeOfDay.DAY;
    public int dayCycleTick = 0;

    public static final int PHASE_DURATION = 3;

    public EventLog<String> messageLog = new EventLog<>(200);

    public void log(String msg) {
        messageLog.add(msg);
    }

    /** Returns cloned copies of all living creatures, independent of the live state. */
    public List<Creature> snapshotCreatures() {
        List<Creature> snapshot = new ArrayList<>(creatures.size());
        for (Creature c : creatures) {
            if (c.isAlive()) snapshot.add(c.clone());
        }
        return snapshot;
    }

    /** Returns the first living creature at (row, col), or null. */
    public Creature getCreatureAt(int row, int col) {
        for (Creature c : creatures) {
            if (c.isAlive() && c.row == row && c.col == col) return c;
        }
        return null;
    }

    /** Returns every living attacker at (row, col). */
    public List<Creature> getAttackersAt(int row, int col) {
        List<Creature> res = new ArrayList<>();
        for (Creature c : creatures) {
            if (c.isAlive() && c.isAttacker() && c.row == row && c.col == col) res.add(c);
        }
        return res;
    }

    /** Returns true if no living creature occupies the cell. */
    public boolean isCellEmpty(int row, int col) {
        return getCreatureAt(row, col) == null;
    }

    /** Returns true if a living defender occupies the cell. */
    public boolean hasDefender(int row, int col) {
        Creature c = getCreatureAt(row, col);
        return c != null && c.isDefender();
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    /** Returns all living attackers. */
    public List<Creature> livingAttackers() {
        List<Creature> res = new ArrayList<>();
        for (Creature c : creatures) {
            if (c.isAlive() && c.isAttacker()) res.add(c);
        }
        return res;
    }

    /** Returns all living defenders. */
    public List<Creature> livingDefenders() {
        List<Creature> res = new ArrayList<>();
        for (Creature c : creatures) {
            if (c.isAlive() && c.isDefender()) res.add(c);
        }
        return res;
    }

    /** Removes all dead creatures from the list. */
    public void purgeDeadCreatures() {
        creatures.removeIf(c -> !c.isAlive());
    }

    /** Advances the day/night cycle by one tick. Returns true if the phase just changed. */
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
