package com.wildhabitat.model;

public enum CreatureType {
    
    WOLF       ("Wolf",        "Wf", "🐺", "#e8d5a3", CreatureRole.ATTACKER,  60, 0.80f, 10, 1,  0),
    RABBIT     ("Rabbit",      "Rb", "🐰", "#f48fb1", CreatureRole.ATTACKER,  30, 1.50f,  6, 1,  0),
    BOAR       ("Boar",        "Bo", "🐗", "#a1887f", CreatureRole.ATTACKER, 120, 0.45f, 20, 1,  0),
    NIGHTSTALKER("NightStalker","NS","👁",  "#ef5350", CreatureRole.ATTACKER,  80, 1.20f, 25, 1,  0),
    SWAMPCRAWLER("SwampCrawler","SC","🐛", "#aed581", CreatureRole.ATTACKER,  70, 0.70f, 15, 1,  0),

    THORNBUSH  ("Thornbush",   "Tb", "🌿", "#5a9e4b", CreatureRole.DEFENDER,  60, 0,     12, 1, 20),
    NIGHTOWL   ("NightOwl",    "NO", "🦉", "#b39ddb", CreatureRole.DEFENDER,  50, 0,     10, 3, 25),
    BATDEFENDER("BatDefender", "Bt", "🦇", "#90caf9", CreatureRole.DEFENDER,  40, 0,      8, 2, 20),
    STONEGUARD ("StoneGuard",  "SG", "🗿", "#b0bec5", CreatureRole.DEFENDER, 100, 0,     18, 1, 30),
    REEDWARDEN ("ReedWarden",  "RW", "🌾", "#80cbc4", CreatureRole.DEFENDER,  60, 0,     10, 1, 15);

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

    CreatureType(String displayName, String abbrev, String emoji, String colorHex,
                 CreatureRole role, int baseHealth, float baseMoveFraction,
                 int basePower, int attackRange, int energyCost) {
                    
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
