package com.wildhabitat.model;

/**
 * Abstract base for every entity on the grid.
 *
 * Implements:
 *   Combatant   — getEffectivePower / canActDuringPhase (abstract, overridden by subclasses)
 *   Cloneable   — clone() produces a shallow copy (safe: all fields are primitives or enums)
 *   Comparable  — natural ordering by current health, used for targeting (weakest / strongest)
 *
 * Concrete subclasses:
 *   AttackerCreature  — movement budget, attacker-specific power scaling
 *   DefenderCreature  — placement cost, defender-specific power scaling
 *
 * Use the static factory Creature.create() instead of constructing subclasses directly.
 */
public abstract class Creature implements Combatant, Cloneable, Comparable<Creature> {

    public final CreatureType type;
    public int row;
    public int col;
    public int health;
    public final int maxHealth;

    private int slowTurns = 0;

    // ── Constructors (package-private — use the factory) ─────────────────────

    protected Creature(CreatureType type, int row, int col) {
        this.type      = type;
        this.row       = row;
        this.col       = col;
        this.health    = type.baseHealth;
        this.maxHealth = type.baseHealth;
    }

    protected Creature(CreatureType type, int row, int col, int health) {
        this(type, row, col);
        this.health = health;
    }

    // ── Static factory ────────────────────────────────────────────────────────

    /** Creates the correct concrete subtype based on the creature's role. */
    public static Creature create(CreatureType type, int row, int col) {
        return type.isAttacker()
                ? new AttackerCreature(type, row, col)
                : new DefenderCreature(type, row, col);
    }

    /** Creates with an explicit health value (used when loading a save file). */
    public static Creature create(CreatureType type, int row, int col, int health) {
        Creature c = create(type, row, col);
        c.health = health;
        return c;
    }

    // ── Combatant interface (abstract — subclasses provide behaviour) ─────────

    @Override
    public abstract int getEffectivePower(TimeOfDay phase);

    @Override
    public abstract boolean canActDuringPhase(TimeOfDay phase);

    // ── Movement (abstract — AttackerCreature uses budget; Defender returns 0) ─

    public abstract int computeMoveCells(TimeOfDay phase, Terrain terrain);

    // ── Comparable — natural order by health (low → high) ────────────────────

    /**
     * Compares by current health so Collections.min/max can find the
     * weakest (NightOwl target) or strongest (StoneGuard target) in one call.
     */
    @Override
    public int compareTo(Creature other) {
        return Integer.compare(this.health, other.health);
    }

    // ── Cloneable ─────────────────────────────────────────────────────────────

    /**
     * Returns a shallow copy.  All fields are primitives or final enum refs,
     * so a shallow copy is a complete, independent snapshot of this creature's state.
     */
    @Override
    public Creature clone() {
        try {
            return (Creature) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // cannot happen — we implement Cloneable
        }
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public boolean isAlive()    { return health > 0; }
    public boolean isAttacker() { return type.role == CreatureRole.ATTACKER; }
    public boolean isDefender() { return type.role == CreatureRole.DEFENDER; }
    public boolean isSlowed()   { return slowTurns > 0; }

    // ── Damage & slow effects ─────────────────────────────────────────────────

    public void takeDamage(int amount) {
        health = Math.max(0, health - amount);
    }

    public void applySlow(int turns) {
        slowTurns = Math.max(slowTurns, turns);
    }

    public void tickSlowEffect() {
        if (slowTurns > 0) slowTurns--;
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return type.displayName + "@(" + row + "," + col + ") HP=" + health;
    }
}
