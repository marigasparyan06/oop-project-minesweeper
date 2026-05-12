package com.wildhabitat.model;

/**
 * Concrete attacker: moves across the grid each turn using a fractional
 * movement budget, deals phase-scaled damage, and can be slowed.
 *
 * Extends the abstract Creature (inheritance + polymorphism).
 */
public class AttackerCreature extends Creature {

    /** Fractional movement accumulator; carries over between turns. */
    private float moveBudget = 0f;

    // ── Construction ──────────────────────────────────────────────────────────

    AttackerCreature(CreatureType type, int row, int col) {
        super(type, row, col);
    }

    // ── Combatant interface ───────────────────────────────────────────────────

    /** Attacker power: +15% at NIGHT, baseline otherwise. */
    @Override
    public int getEffectivePower(TimeOfDay phase) {
        double mult = (phase == TimeOfDay.NIGHT) ? 1.15 : 1.0;
        return (int) Math.round(type.basePower * mult);
    }

    /** NightStalker is completely frozen during DAY; all other attackers can act. */
    @Override
    public boolean canActDuringPhase(TimeOfDay phase) {
        return !(type == CreatureType.NIGHTSTALKER && phase == TimeOfDay.DAY);
    }

    // ── Movement ──────────────────────────────────────────────────────────────

    /**
     * Accumulates fractional movement each turn and returns how many full cells
     * to move this turn (0, 1, or occasionally 2 for Rabbit at high speed).
     * Must only be called after canActDuringPhase() returns true.
     */
    @Override
    public int computeMoveCells(TimeOfDay phase, Terrain terrain) {
        float fraction = type.baseMoveFraction * (float) phase.attackerSpeedMult();

        // SwampCrawler gets a 30% bonus on water tiles
        if (type == CreatureType.SWAMPCRAWLER && terrain.isWater()) {
            fraction *= 1.3f;
        }

        // Slowed creatures move at half rate
        if (isSlowed()) {
            fraction *= 0.5f;
        }

        moveBudget += fraction;

        int cells = 0;
        while (moveBudget >= 1.0f) {
            ++cells;
            moveBudget -= 1.0f;
        }
        return cells;
    }

    // ── Cloneable override (covariant return) ─────────────────────────────────

    @Override
    public AttackerCreature clone() {
        return (AttackerCreature) super.clone();
    }
}
