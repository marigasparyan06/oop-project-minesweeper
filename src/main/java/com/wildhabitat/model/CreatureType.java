package com.wildhabitat.model;

public enum CreatureType {

    ATTACKER1("Attacker1", "A1", "🐺", "#e8d5a3", CreatureRole.ATTACKER,  60, 0.80f, 10, 1,  0,
              new Terrain[]{Terrain.GRASS, Terrain.STONE}),
    ATTACKER2("Attacker2", "A2", "🐰", "#f48fb1", CreatureRole.ATTACKER,  30, 1.50f,  6, 1,  0,
              new Terrain[]{Terrain.GRASS}),
    ATTACKER3("Attacker3", "A3", "🐗", "#a1887f", CreatureRole.ATTACKER, 120, 0.45f, 20, 1,  0,
              new Terrain[]{Terrain.GRASS, Terrain.STONE}),
    ATTACKER4("Attacker4", "A4", "👁",  "#ef5350", CreatureRole.ATTACKER,  80, 1.20f, 25, 1,  0,
              new Terrain[]{Terrain.GRASS, Terrain.STONE, Terrain.SHALLOW_WATER, Terrain.DEEP_WATER}),
    ATTACKER5("Attacker5", "A5", "🐛", "#aed581", CreatureRole.ATTACKER,  70, 0.70f, 15, 1,  0,
              new Terrain[]{Terrain.SHALLOW_WATER, Terrain.DEEP_WATER}),

    DEFENDER1("Defender1", "D1", "🌿", "#5a9e4b", CreatureRole.DEFENDER,  60, 0,     12, 1, 20,
              new Terrain[]{Terrain.GRASS}),
    DEFENDER2("Defender2", "D2", "🦉", "#b39ddb", CreatureRole.DEFENDER,  50, 0,     10, 3, 25,
              new Terrain[]{Terrain.GRASS, Terrain.STONE}),
    DEFENDER3("Defender3", "D3", "🦇", "#90caf9", CreatureRole.DEFENDER,  40, 0,      8, 2, 20,
              new Terrain[]{Terrain.GRASS, Terrain.STONE, Terrain.SHALLOW_WATER, Terrain.DEEP_WATER}),
    DEFENDER4("Defender4", "D4", "🗿", "#b0bec5", CreatureRole.DEFENDER, 100, 0,     18, 1, 30,
              new Terrain[]{Terrain.GRASS, Terrain.STONE}),
    DEFENDER5("Defender5", "D5", "🌾", "#80cbc4", CreatureRole.DEFENDER,  60, 0,     10, 1, 15,
              new Terrain[]{Terrain.SHALLOW_WATER, Terrain.DEEP_WATER});

    public final String displayName;
    public final String abbrev;
    public final String emoji;
    public final String colorHex;
    public final CreatureRole role;
    public final int baseHealth;
    public final float baseMoveFraction;
    public final int basePower;
    public final int attackRange;
    public final int energyCost;
    public final Terrain[] allowedTerrains;

    CreatureType(String displayName, String abbrev, String emoji, String colorHex,
                 CreatureRole role, int baseHealth, float baseMoveFraction,
                 int basePower, int attackRange, int energyCost, Terrain[] allowedTerrains) {
        this.displayName = displayName;
        this.abbrev = abbrev;
        this.emoji = emoji;
        this.colorHex = colorHex;
        this.role = role;
        this.baseHealth = baseHealth;
        this.baseMoveFraction = baseMoveFraction;
        this.basePower = basePower;
        this.attackRange = attackRange;
        this.energyCost = energyCost;
        this.allowedTerrains = allowedTerrains;
    }

    public boolean isAttacker() { return role == CreatureRole.ATTACKER; }
    public boolean isDefender() { return role == CreatureRole.DEFENDER; }

    /** Returns true if this defender gets a power bonus at night. */
    public boolean isNightSpecialist() { return this == DEFENDER2 || this == DEFENDER3; }

    /** Returns true if this creature type can occupy the given terrain. */
    public boolean canAccessTerrain(Terrain t) {
        for (Terrain allowed : allowedTerrains) {
            if (allowed == t) return true;
        }
        return false;
    }

    public static CreatureType fromName(String name) {
        for (CreatureType t : values()) {
            if (t.displayName.equalsIgnoreCase(name.trim()) || t.abbrev.equalsIgnoreCase(name.trim())) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown creature: " + name);
    }

    public static CreatureType[] defenders() {
        return new CreatureType[]{ DEFENDER1, DEFENDER2, DEFENDER3, DEFENDER4, DEFENDER5 };
    }
}
