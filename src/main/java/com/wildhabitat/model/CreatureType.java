package com.wildhabitat.model;

public enum CreatureType {

    WOLF       ("Wolf",        "Wf", "🐺", "#e8d5a3", CreatureRole.ATTACKER,  60, 0.80f, 10, 1,  0,
                new Terrain[]{Terrain.GRASS, Terrain.STONE}),
    RABBIT     ("Rabbit",      "Rb", "🐰", "#f48fb1", CreatureRole.ATTACKER,  30, 1.50f,  6, 1,  0,
                new Terrain[]{Terrain.GRASS}),
    BOAR       ("Boar",        "Bo", "🐗", "#a1887f", CreatureRole.ATTACKER, 120, 0.45f, 20, 1,  0,
                new Terrain[]{Terrain.GRASS, Terrain.STONE}),
    NIGHTSTALKER("NightStalker","NS","👁",  "#ef5350", CreatureRole.ATTACKER,  80, 1.20f, 25, 1,  0,
                new Terrain[]{Terrain.GRASS, Terrain.STONE, Terrain.SHALLOW_WATER, Terrain.DEEP_WATER}),
    SWAMPCRAWLER("SwampCrawler","SC","🐛", "#aed581", CreatureRole.ATTACKER,  70, 0.70f, 15, 1,  0,
                new Terrain[]{Terrain.SHALLOW_WATER, Terrain.DEEP_WATER}),

    THORNBUSH  ("Thornbush",   "Tb", "🌿", "#5a9e4b", CreatureRole.DEFENDER,  60, 0,     12, 1, 20,
                new Terrain[]{Terrain.GRASS}),
    NIGHTOWL   ("NightOwl",    "NO", "🦉", "#b39ddb", CreatureRole.DEFENDER,  50, 0,     10, 3, 25,
                new Terrain[]{Terrain.GRASS, Terrain.STONE}),
    BATDEFENDER("BatDefender", "Bt", "🦇", "#90caf9", CreatureRole.DEFENDER,  40, 0,      8, 2, 20,
                new Terrain[]{Terrain.GRASS, Terrain.STONE, Terrain.SHALLOW_WATER, Terrain.DEEP_WATER}),
    STONEGUARD ("StoneGuard",  "SG", "🗿", "#b0bec5", CreatureRole.DEFENDER, 100, 0,     18, 1, 30,
                new Terrain[]{Terrain.GRASS, Terrain.STONE}),
    REEDWARDEN ("ReedWarden",  "RW", "🌾", "#80cbc4", CreatureRole.DEFENDER,  60, 0,     10, 1, 15,
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

    public boolean isAttacker() {
        return role == CreatureRole.ATTACKER;
    }

    public boolean isDefender() {
        return role == CreatureRole.DEFENDER;
    }

    public boolean isNightSpecialist() {
        return this == NIGHTOWL || this == BATDEFENDER;
    }

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
        return new CreatureType[]{ THORNBUSH, NIGHTOWL, BATDEFENDER, STONEGUARD, REEDWARDEN };
    }
}
