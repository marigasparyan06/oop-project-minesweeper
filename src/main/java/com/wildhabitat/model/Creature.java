package com.wildhabitat.model;

/**
 * Abstract base for every entity on the grid.
 *
 * Implements Combatant (getEffectivePower / canActDuringPhase), Cloneable,
 * and Comparable (natural ordering by current health, used for targeting).
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

    /** Creates the correct concrete subtype based on the creature's role. */
    public static Creature create(CreatureType type, int row, int col) {
        return type.isAttacker() ? new AttackerCreature(type, row, col) : new DefenderCreature(type, row, col);
    }

    /** Creates with an explicit health value, used when loading a save file. */
    public static Creature create(CreatureType type, int row, int col, int health) {
        Creature c = create(type, row, col);
        c.health = health;
        return c;
    }

    @Override
    public abstract int getEffectivePower(TimeOfDay phase);

    @Override
    public abstract boolean canActDuringPhase(TimeOfDay phase);

    public abstract int computeMoveCells(TimeOfDay phase, Terrain terrain);

    /** Orders by current health so Collections.min/max can find the weakest or strongest target. */
    @Override
    public int compareTo(Creature other) {
        return Integer.compare(this.health, other.health);
    }

    /** Returns a shallow copy. Safe because all fields are primitives or final enum refs. */
    @Override
    public Creature clone() {
        try {
            return (Creature) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public boolean isAlive()    { return health > 0; }
    public boolean isAttacker() { return type.role == CreatureRole.ATTACKER; }
    public boolean isDefender() { return type.role == CreatureRole.DEFENDER; }
    public boolean isSlowed()   { return slowTurns > 0; }

    public void takeDamage(int amount) {
        health = Math.max(0, health - amount);
    }

    public void applySlow(int turns) {
        slowTurns = Math.max(slowTurns, turns);
    }

    public void tickSlowEffect() {
        if (slowTurns > 0) slowTurns--;
    }

    @Override
    public String toString() {
        return type.displayName + "@(" + row + "," + col + ") HP=" + health;
    }
}
