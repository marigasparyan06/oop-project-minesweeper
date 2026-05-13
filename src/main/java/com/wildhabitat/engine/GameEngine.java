package com.wildhabitat.engine;

import com.wildhabitat.exception.InvalidPlacementException;
import com.wildhabitat.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Core game logic. Call nextTurn() once per step to advance the simulation.
 * placeDefender() throws InvalidPlacementException on illegal moves.
 * defendersAttack() uses Collections.min/max via Creature's Comparable implementation.
 */
public class GameEngine {

    public static final int ENERGY_PER_TURN   = 10;
    public static final int SCORE_PER_KILL    = 10;
    public static final int SCORE_WAVE_CLEAR  = 50;
    public static final int ENERGY_WAVE_CLEAR = 20;
    public static final int NIGHT_ENERGY_DISC = 15;
    public static final int MAX_WAVES         = 10;

    private final GameState state;

    private final List<int[]> attackedCells = new ArrayList<>();
    private final List<int[]> movedCells    = new ArrayList<>();
    private boolean phaseChangedThisTurn    = false;
    private boolean gameOver                = false;
    private boolean playerWon              = false;
    private int     waveCooldown           = 0;

    public GameEngine(GameState state) {
        this.state = state;
    }

    public GameState      getState()                  { return state; }
    public boolean        isGameOver()                { return gameOver; }
    public boolean        didPlayerWin()              { return playerWon; }
    public List<int[]>    getLastAttackedCells()      { return attackedCells; }
    public List<int[]>    getLastMovedCells()         { return movedCells; }
    public boolean        wasPhaseChangedThisTurn()   { return phaseChangedThisTurn; }

    /** Advances the simulation by one turn and returns the messages generated. */
    public List<String> nextTurn() {
        if (gameOver) return List.of("[GAME] Game is already over.");

        state.messageLog.clear();
        attackedCells.clear();
        movedCells.clear();
        phaseChangedThisTurn = false;

        phaseChangedThisTurn = state.tickDayCycle();
        if (phaseChangedThisTurn) state.log(state.timeOfDay.transitionMessage());

        state.energy += ENERGY_PER_TURN;

        for (Creature c : state.creatures) {
            if (c.isAlive()) c.tickSlowEffect();
        }

        moveAttackers();
        if (gameOver) return new ArrayList<>(state.messageLog);

        attackersAttack();
        defendersAttack();
        purgeAndScore();
        handleWaveProgress();

        ++state.turn;
        return new ArrayList<>(state.messageLog);
    }

    /**
     * Places a defender on the grid.
     * Throws InvalidPlacementException if the move is illegal (wrong type, out of bounds,
     * wrong terrain, occupied cell, or not enough energy).
     */
    public String placeDefender(CreatureType type, int row, int col) {
        if (!type.isDefender()) {
            throw new InvalidPlacementException(type.displayName + " is not a defender.");
        }
        if (!state.inBounds(row, col)) {
            throw new InvalidPlacementException(
                    "Position (" + row + "," + col + ") is out of bounds.");
        }
        if (!state.isCellEmpty(row, col)) {
            throw new InvalidPlacementException(
                    "Cell (" + row + "," + col + ") is already occupied.");
        }
        Terrain cellTerrain = state.terrain[row][col];
        if (!type.canAccessTerrain(cellTerrain)) {
            throw new InvalidPlacementException(
                    type.displayName + " cannot be placed on " + cellTerrain + " terrain.");
        }

        int cost = effectiveCost(type);
        if (state.energy < cost) {
            throw new InvalidPlacementException(
                    "Not enough energy (" + state.energy + " < " + cost + ").");
        }

        state.energy -= cost;
        state.creatures.add(Creature.create(type, row, col));

        return "[PLACE] Placed " + type.displayName + " at (" + row + "," + col
                + ") for " + cost + " energy.";
    }

    /** Returns the energy cost for a defender, with a NIGHT discount applied if active. */
    public int effectiveCost(CreatureType type) {
        if (!type.isDefender()) return 0;
        int base = type.energyCost;
        if (state.timeOfDay == TimeOfDay.NIGHT) {
            base = (int) Math.round(base * (1.0 - NIGHT_ENERGY_DISC / 100.0));
        }
        return base;
    }

    /** Spawns the next wave immediately. Attackers are placed on terrain-compatible rows. */
    public void triggerNextWave() {
        List<Creature> wave = WaveManager.buildWave(state.wave);
        for (Creature c : wave) {
            int attempts = 0;
            while (attempts < GameState.ROWS * 2) {
                Terrain rowTerrain = state.terrain[c.row][0];
                if (c.type.canAccessTerrain(rowTerrain) && state.getCreatureAt(c.row, 0) == null) {
                    break;
                }
                c.row = (c.row + 1) % GameState.ROWS;
                attempts++;
            }
            state.creatures.add(c);
        }
        state.log("[WAVE] Wave " + state.wave + " incoming! (" + wave.size() + " creatures)");
        state.wave++;
        waveCooldown = 0;
    }

    private void moveAttackers() {
        for (Creature a : state.livingAttackers()) {
            if (!a.isAlive()) continue;
            if (!a.canActDuringPhase(state.timeOfDay)) continue;

            Terrain t = state.terrain[a.row][a.col];
            int cells = a.computeMoveCells(state.timeOfDay, t);
            if (cells == 0) continue;

            for (int step = 0; step < cells; step++) {
                int nextCol = a.col + 1;
                if (nextCol >= GameState.COLS) {
                    state.log("[DEFEAT] " + a.type.displayName + " breached the habitat! Game over.");
                    a.health = 0;
                    gameOver  = true;
                    playerWon = false;
                    return;
                }
                a.col = nextCol;
                movedCells.add(new int[]{a.row, a.col});
            }
        }
    }

    private void attackersAttack() {
        for (Creature attacker : state.livingAttackers()) {
            Creature target = null;
            for (int dc = 0; dc <= 1; dc++) {
                int checkCol = attacker.col + dc;
                if (!state.inBounds(attacker.row, checkCol)) continue;
                Creature c = state.getCreatureAt(attacker.row, checkCol);
                if (c != null && c.isDefender()) { target = c; break; }
            }
            if (target == null) continue;

            int power = attacker.getEffectivePower(state.timeOfDay);
            target.takeDamage(power);
            attackedCells.add(new int[]{target.row, target.col});
            state.log("[ATTACK] " + attacker.type.abbrev + " → " + target.type.abbrev
                    + " for " + power + " dmg (hp left: " + target.health + ")");
        }
    }

    private void defendersAttack() {
        for (Creature def : state.livingDefenders()) {
            int range = def.type.attackRange;
            int power = def.getEffectivePower(state.timeOfDay);

            Terrain terrain = state.terrain[def.row][def.col];

            if (def.type == CreatureType.DEFENDER5 && terrain.isWater()) {
                power = (int) (power * 1.2);
            }

            List<Creature> targets = getTargets(def, range);
            if (targets.isEmpty()) continue;

            switch (def.type) {
                case DEFENDER1:
                case DEFENDER3:
                case DEFENDER5:
                    for (Creature t : targets) dealDamage(def, t, power, terrain);
                    break;

                case DEFENDER4:
                    dealDamage(def, Collections.max(targets), power * 2, terrain);
                    break;

                case DEFENDER2:
                    dealDamage(def, Collections.min(targets), power, terrain);
                    break;

                default:
                    break;
            }
        }
    }

    private List<Creature> getTargets(Creature def, int range) {
        List<Creature> targets = new ArrayList<>();
        for (Creature c : state.livingAttackers()) {
            int dr = Math.abs(c.row - def.row);
            int dc = Math.abs(c.col - def.col);
            if (Math.max(dr, dc) <= range) targets.add(c);
        }
        return targets;
    }

    private void dealDamage(Creature def, Creature target, int power, Terrain defTerrain) {
        target.takeDamage(power);
        attackedCells.add(new int[]{target.row, target.col});
        state.log("[ATTACK] " + def.type.abbrev + " → " + target.type.abbrev
                + " for " + power + " dmg (hp left: " + target.health + ")");
        if (def.type == CreatureType.DEFENDER1) target.applySlow(1);
    }

    private void purgeAndScore() {
        int attackersKilled = 0;
        int defendersFallen = 0;
        for (Creature c : state.creatures) {
            if (!c.isAlive()) {
                if (c.isAttacker()) ++attackersKilled;
                else                ++defendersFallen;
            }
        }
        if (attackersKilled > 0) {
            state.score += attackersKilled * SCORE_PER_KILL;
            state.log("[DEFEND] " + attackersKilled + " attacker(s) defeated! +"
                    + (attackersKilled * SCORE_PER_KILL) + " pts");
        }
        if (defendersFallen > 0) {
            state.log("[LOSS] " + defendersFallen + " defender(s) lost!");
        }
        state.purgeDeadCreatures();
    }

    private void handleWaveProgress() {
        if (gameOver) return;
        if (!state.livingAttackers().isEmpty()) { waveCooldown = 0; return; }

        ++waveCooldown;
        if (waveCooldown >= 2) {
            state.score  += SCORE_WAVE_CLEAR;
            state.energy += ENERGY_WAVE_CLEAR;
            state.log("[WAVE] Wave cleared! +" + SCORE_WAVE_CLEAR + " score, +"
                    + ENERGY_WAVE_CLEAR + " energy.");

            if (state.wave > MAX_WAVES) {
                gameOver  = true;
                playerWon = true;
                state.log("[WIN] All " + MAX_WAVES + " waves cleared! Final score: " + state.score);
                return;
            }
            triggerNextWave();
            waveCooldown = 0;
        }
    }
}
