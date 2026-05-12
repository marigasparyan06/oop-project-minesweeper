public class Creature {
    public enum Type {
       
        Wolf, Rabbit, Boar, NightStalker, SwampCrawler,
        
        Thornbush, NightOwl, BatDefender, StoneGuard, ReedWarden
    }

    public enum Role { ATTACKER, DEFENDER }

    private static final String[] ABBREV = {
        "Wf", "Rb", "Bo", "NS", "SC",   
        "Tb", "NO", "Bt", "SG", "RW"    
    };

    private static final int[] BASE_HP = {
        60, 30, 90, 50, 45,   
        40, 35, 30, 80, 45    
    };

    private static final int[] BASE_ATTACK = {
        15, 8, 20, 18, 12,    
        10, 14, 12, 8, 11     
    };

    private static final int[] BASE_COST = {
        0, 0, 0, 0, 0,        
        20, 25, 20, 30, 15    
    };

    private Type type;
    private int row, col;
    private int hp;
    private int maxHp;
    private boolean alive;

    public Creature(Type type, int row, int col) {
        this.type = type;
        this.row = row;
        this.col = col;
        this.maxHp = BASE_HP[type.ordinal()];
        this.hp = this.maxHp;
        this.alive = true;
    }


    public Creature(Type type, int row, int col, int hp) {
        this(type, row, col);
        this.hp = Math.min(hp, this.maxHp);
    }

    public Type getType() { return type; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public void setRow(int r) { row = r; }
    public void setCol(int c) { col = c; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public boolean isAlive() { return alive; }

    public Role getRole() {
        return type.ordinal() < 5 ? Role.ATTACKER : Role.DEFENDER;
    }

    public int getBaseAttack() { return BASE_ATTACK[type.ordinal()]; }

    public int getPlacementCost() { return BASE_COST[type.ordinal()]; }

    public String getAbbrev() { return ABBREV[type.ordinal()]; }

    
    public boolean takeDamage(int amount) {
        hp = Math.max(0, hp - amount);
        if (hp == 0) alive = false;
        return !alive;
    }

    
    public int getEffectiveAttack(DayNightCycle.Phase phase) {
        double mult = 1.0;
        if (getRole() == Role.ATTACKER) {
            if (phase == DayNightCycle.Phase.NIGHT) mult = 1.15;
            else if (phase == DayNightCycle.Phase.DUSK) mult = 1.10;
        } else {
            if (phase == DayNightCycle.Phase.DAWN) mult = 1.10;
            else if (phase == DayNightCycle.Phase.NIGHT) {
              
                if (type != Type.NightOwl && type != Type.BatDefender)
                    mult = 0.90;
            }
        }
        if (type == Type.NightStalker && phase == DayNightCycle.Phase.DAY)
            return 0;
        return (int)(getBaseAttack() * mult);
    }

    
    public double getSpeedMultiplier(DayNightCycle.Phase phase) {
        double mult = 1.0;
        if (getRole() == Role.ATTACKER) {
            if (phase == DayNightCycle.Phase.DAWN) mult = 0.75;
            else if (phase == DayNightCycle.Phase.DUSK) mult = 1.10;
            else if (phase == DayNightCycle.Phase.NIGHT) mult = 1.25;
        }
        if (type == Type.Rabbit && phase == DayNightCycle.Phase.DAWN) mult *= 0.8;
        if (type == Type.NightStalker && phase == DayNightCycle.Phase.DAY) return 0;
        return mult;
    }

    @Override
    public String toString() {
        return type.name() + "@(" + row + "," + col + ") hp=" + hp;
    }
}
