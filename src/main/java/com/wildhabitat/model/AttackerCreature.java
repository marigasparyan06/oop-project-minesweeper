package com.wildhabitat.model;

/**
 * Concrete attacker: moves across the grid each turn using a fractional
 * movement budget, deals phase-scaled damage, and can be slowed.
 */
public class AttackerCreature extends Creature {

    private float moveBudget = 0f;

    AttackerCreature(CreatureType type, int row, int col) {
        super(type, row, col);
    }

    /** Returns +15% power at NIGHT, baseline otherwise. */
    @Override
    public int getEffectivePower(TimeOfDay phase) {
        double mult = (phase == TimeOfDay.NIGHT) ? 1.15 : 1.0;
        return (int) Math.round(type.basePower * mult);
    }

    /** Attacker4 is frozen during DAY; all other attackers can always act. */
    @Override
    public boolean canActDuringPhase(TimeOfDay phase) {
        return !(type == CreatureType.ATTACKER4 && phase == TimeOfDay.DAY);
    }

    /**
     * Accumulates fractional movement each turn and returns how many full cells to move.
     * Must only be called after canActDuringPhase() returns true.
     */
    @Override
    public int computeMoveCells(TimeOfDay phase, Terrain terrain) {
        float fraction = type.baseMoveFraction * (float) phase.attackerSpeedMult();

        if (type == CreatureType.ATTACKER5 && terrain.isWater()) {
            fraction *= 1.3f;
        }

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

    @Override
    public AttackerCreature clone() {
        return (AttackerCreature) super.clone();
    }
}
