package com.wildhabitat.model;

/**
 * Combat capability shared by every entity that can fight on the grid.
 * Implemented by the abstract Creature hierarchy.
 */
public interface Combatant {

    /** Effective attack power for this entity given the current phase. */
    int getEffectivePower(TimeOfDay phase);

    /**
     * Whether this entity is allowed to act (move or attack) during the given phase.
     * NightStalker returns false during DAY; all other creatures return true.
     */
    boolean canActDuringPhase(TimeOfDay phase);
}
