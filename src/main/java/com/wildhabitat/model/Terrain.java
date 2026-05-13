package com.wildhabitat.model;

public enum Terrain {
    GRASS, STONE, SHALLOW_WATER, DEEP_WATER;

    public String toCode() {
        switch (this) {
            case GRASS: return "GR";
            case STONE: return "ST";
            case SHALLOW_WATER: return "SW";
            case DEEP_WATER: return "DW";
            default: return "??";
        }
    }

    public boolean isWater() {
        return this == SHALLOW_WATER || this == DEEP_WATER;
    }

    /** Returns the hex color used by the UI renderer for this terrain. */
    public String colorHex() {
        switch (this) {
            case GRASS:         return "#4a7c59";
            case STONE:         return "#8a8a8a";
            case SHALLOW_WATER: return "#7ec8e3";
            case DEEP_WATER:    return "#1a6b9a";
            default:            return "#333333";
        }
    }

    public static Terrain fromString(String s) {
        switch (s.trim().toUpperCase()) {
            case "GRASS": return GRASS;
            case "STONE": return STONE;
            case "SHALLOW_WATER": return SHALLOW_WATER;
            case "DEEP_WATER": return DEEP_WATER;
            default: throw new IllegalArgumentException("Unknown terrain: " + s);
        }
    }
}
