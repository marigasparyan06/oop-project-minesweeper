package com.wildhabitat.model;

/**
 * Concrete defender: placed by the player, never moves, attacks nearby enemies.
 * All phase-based power scaling for defenders lives here.
 */
public class DefenderCreature extends Creature {

    DefenderCreature(CreatureType type, int row, int col) {
        super(type, row, col);
    }

    /**
     * Returns defender power scaled by phase and specialization:
     * DAWN gives +10% to all defenders.
     * NIGHT gives -10% to standard defenders, but +25% to NightOwl and BatDefender.
     * NightOwl operates at 40% power during DAY; BatDefender at 15%.
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

    /** Defenders can always act regardless of phase. */
    @Override
    public boolean canActDuringPhase(TimeOfDay phase) {
        return true;
    }

    /** Defenders are stationary and never move. */
    @Override
    public int computeMoveCells(TimeOfDay phase, Terrain terrain) {
        return 0;
    }

    @Override
    public DefenderCreature clone() {
        return (DefenderCreature) super.clone();
    }
}
