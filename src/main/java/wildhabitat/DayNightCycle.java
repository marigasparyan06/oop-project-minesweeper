package main.java.wildhabitat;

/**
 * Manages the 4-phase day/night cycle.
 * Every 6 turns cycles through DAWN -> DAY -> DUSK -> NIGHT.
 * Each phase lasts 3 turns.
 */
public class DayNightCycle {
    public enum Phase { DAWN, DAY, DUSK, NIGHT }

    private static final Phase[] ORDER = { Phase.DAWN, Phase.DAY, Phase.DUSK, Phase.NIGHT };
    private static final int TURNS_PER_PHASE = 3;

    private Phase currentPhase;
    private int ticksInPhase; // how many turns elapsed in current phase

    public DayNightCycle() {
        currentPhase = Phase.DAY;
        ticksInPhase = 0;
    }

    public DayNightCycle(Phase phase, int tick) {
        this.currentPhase = phase;
        this.ticksInPhase = tick;
    }

    public Phase getPhase() { 
        return currentPhase; 
    }

    public int getTicksInPhase() { 
        return ticksInPhase; 
    }

    /**
     * Advance one turn. Returns true if a phase transition occurred.
     */
    public boolean tick() {
        ++ticksInPhase;
        
        if (ticksInPhase >= TURNS_PER_PHASE) {
            ticksInPhase = 0;
           
            int nextIdx = (phaseIndex() + 1) % ORDER.length;
            
            currentPhase = ORDER[nextIdx];
            
            return true;
        }

        return false;
    }

    private int phaseIndex() {
        for (int i = 0; i < ORDER.length; ++i) {
            if (ORDER[i] == currentPhase) {
                return i;
            } 

        }

        return 0;
    }

    /** Display label for the top bar. */
    public String getLabel() {
        switch (currentPhase) {
            case DAWN:  
                return "🌅 DAWN";
            case DAY:   
                return "☀ DAY";
            case DUSK:  
                return "🌆 DUSK";
            case NIGHT: 
                return "🌙 NIGHT";
            default:    
                return currentPhase.name();
        }
    }

    /** Hex color string for top bar background (for CLI display, just name). */
    public String getTopBarColor() {
        switch (currentPhase) {
            case DAY:   
                return "#87CEEB";
            case DAWN:  
                return "#FF7F50";
            case DUSK:  
                return "#FFBF00";
            case NIGHT: 
                return "#0d0d2b";
            default:    
                return "#87CEEB";
        }
    }

    /** Phase transition announcement message. */
    public String getTransitionMessage() {
        switch (currentPhase) {
            case DAWN:  
                return "[PHASE] Dawn breaks. Defenders gain +10% power. Attackers slow to 75%.";
            case DAY:   
                return "[PHASE] Day arrives. Baseline stats restored. Full grid visibility.";
            case DUSK:  
                return "[PHASE] Dusk falls. Attackers speed up +10%. Amber light settles.";
            case NIGHT: 
                return "[PHASE] Night descends. Attackers +25% speed, +15% attack. Defenders -10% effectiveness.";
            default:    
                return "[PHASE] Phase changed.";
        }
    }
}

