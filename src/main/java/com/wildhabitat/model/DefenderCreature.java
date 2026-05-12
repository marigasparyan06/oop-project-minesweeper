package com.wildhabitat.model;

/**
 * Concrete defender: placed by the player, never moves, attacks nearby enemies.
 *
 * Extends the abstract Creature (inheritance + polymorphism).
 * All phase-based power scaling for defenders lives here, keeping GameEngine clean.
 */
public class DefenderCreature extends Creature {

    // ── Construction ──────────────────────────────────────────────────────────

    DefenderCreature(CreatureType type, int row, int col) {
        super(type, row, col);
    }

    // ── Combatant interface ───────────────────────────────────────────────────

    /**
     * Defender power with full phase and specialist scaling:
     *   DAWN           → +10% for all defenders
     *   NIGHT          → -10% for standard defenders; +25% for NightOwl/BatDefender
     *   DAY/NightOwl   → 40% (nearly idle during daylight)
     *   DAY/BatDefender→ 15% (almost invisible during daylight)
     */
    @Override
    public int getEffectivePower(TimeOfDay phase) {
        double mult = phase.defenderPowerMult();

        if (phase == TimeOfDay.NIGHT && type.isNightSpecialist()) {
            mult = 1.25;
        }
        if (phase == TimeOfDay.DAY && type == CreatureType.NIGHTOWL) {
            mult = 0.4;
        }
        if (phase == TimeOfDay.DAY && type == CreatureType.BATDEFENDER) {
            mult = 0.15;
        }

        return (int) Math.round(type.basePower * mult);
    }

    /** Defenders can always act regardless of the phase. */
    @Override
    public boolean canActDuringPhase(TimeOfDay phase) {
        return true;
    }

    /** Defenders are stationary — movement is always zero. */
    @Override
    public int computeMoveCells(TimeOfDay phase, Terrain terrain) {
        return 0;
    }

    // ── Cloneable override (covariant return) ─────────────────────────────────

    @Override
    public DefenderCreature clone() {
        return (DefenderCreature) super.clone();
    }
}
