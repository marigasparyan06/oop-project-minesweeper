package com.wildhabitat.model;

public enum TimeOfDay {
    DAWN, DAY, DUSK, NIGHT;

    /** Cyclic order: DAWN → DAY → DUSK → NIGHT → DAWN */
    public TimeOfDay next() {
        switch (this) {
            case DAWN:  
                return DAY;
            case DAY:   
                return DUSK;
            case DUSK:  
                return NIGHT;
            case NIGHT: 
                return DAWN;
            default:    
                return DAY;
        }
    }

    /** Emoji + name label shown in the top bar. */
    public String label() {
        switch (this) {
            case DAWN:  
                return "🌅 DAWN";
            case DAY:   
                return "☀ DAY";
            case DUSK:  
                return "🌆 DUSK";
            case NIGHT: 
                return "🌙 NIGHT";
            default:    
                return name();
        }
    }

    /** Plain name without emoji — used by CLI header and save files. */
    public String shortName() { 
        return name(); 
    }

    /**
     * Top bar background color hex for this phase.
     * Used by the UI to animate the header strip.
     */
    public String topBarHex() {
        switch (this) {
            case DAWN:  
                return "#c0533a"; // coral
            case DAY:   
                return "#87CEEB"; // sky blue
            case DUSK:  
                return "#b35900"; // amber
            case NIGHT: 
                return "#0d0d2b"; // deep indigo
            default:    
                return "#87CEEB";
        }
    }

    /**
     * Phase modifier applied to ALL attacker speeds.
     * Actual movement calculated in Creature.computeMoveCells().
     */
    public double attackerSpeedMult() {
        switch (this) {
            case DAWN:  
                return 0.75;
            case DAY:   
                return 1.0;
            case DUSK:  
                return 1.1;
            case NIGHT: 
                return 1.25;
            default:    
                return 1.0;
        }
    }

    /**
     * Phase modifier applied to defender attack power.
     * Night-specialist defenders (NightOwl, BatDefender) override the NIGHT value.
     */
    public double defenderPowerMult() {
        switch (this) {
            case DAWN:  
                return 1.10;
            case NIGHT: 
                return 0.90;
            default:    
                return 1.0;
        }
    }

    /** One-line announcement logged on phase transition. */
    public String transitionMessage() {
        switch (this) {
            case DAWN:  
                return "[PHASE] Dawn breaks. Defenders +10% power. Attackers slow to 75%.";
            case DAY:   
                return "[PHASE] Day arrives. Baseline stats restored. Full grid visibility.";
            case DUSK:  
                return "[PHASE] Dusk falls. Attackers +10% speed. Amber light settles.";
            case NIGHT: 
                return "[PHASE] Night descends. Attackers +25% speed, +15% attack. Defenders -10% effectiveness.";
            default:    
                return "[PHASE] Phase changed to " + name() + ".";
        }
    }

    /** Single-line note printed under the CLI header each turn. */
    public String phaseNote() {
        switch (this) {
            case DAWN:  
                return "[*DAWN*]  Attackers at 75% speed. Defenders +10% power.";
            case DAY:   
                return "[*DAY*]   Baseline stats. Full visibility.";
            case DUSK:  
                return "[*DUSK*]  Attackers +10% speed. Amber light.";
            case NIGHT: 
                return "[*NIGHT*] NightStalkers may spawn. Defender placement -15% energy cost.";
            default:    
                return "";
        }
    }

    public static TimeOfDay fromString(String s) {
        switch (s.trim().toUpperCase()) {
            case "DAWN":  
                return DAWN;
            case "DAY":   
                return DAY;
            case "DUSK":  
                return DUSK;
            case "NIGHT": 
                return NIGHT;
            default: 
                throw new IllegalArgumentException("Unknown phase: " + s);
        }
    }
}
