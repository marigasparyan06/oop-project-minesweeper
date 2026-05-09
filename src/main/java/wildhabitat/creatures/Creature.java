package wildhabitat.creatures;

import wildhabitat.board.Board;
import wildhabitat.enums.TileType;
import wildhabitat.exceptions.CreatureDeadException;

public abstract class Creature {

    protected int x;           // row
    protected int y;           // col
    protected int energy;
    protected int visionRange;
    protected CreatureStats stats;
    protected Board board;     // set by Board.placeCreature()

    // Stun state shared by both Defenders (Bear roar) and Attackers
    protected boolean stunned = false;
    protected int stunTurnsLeft = 0;

    public Creature(int x, int y, int energy, int visionRange) {
        this.x = x;
        this.y = y;

        this.energy = energy;
        this.visionRange = visionRange;
        
        this.stats = new CreatureStats(energy);
    }

    public abstract void move();
    public abstract void eat();
    public abstract boolean canEnter(TileType tile);
    public abstract String getAbbreviation();

    public void born() {
        System.out.println(getClass().getSimpleName() + " spawned at (" + x + "," + y + ")");
    }

    public void die() {
        energy = 0;

        System.out.println(getClass().getSimpleName() + " at (" + x + "," + y + ") died. Kills: " + stats.killCount);
    }

    protected void checkAlive() {
        if (energy <= 0) {
            throw new CreatureDeadException(getClass().getSimpleName());
        } 
    }

    public void takeDamage(int amount) {
        energy -= amount;

        if (energy < 0) {
            energy = 0;
        } 
    }

    public boolean isAlive() {
        return energy > 0;
    }

    public void stun(int turns) {
        stunned = true;
        stunTurnsLeft = turns;
    }

    public boolean isStunned() {
        return stunned;
    }

    public void decrementStun() {
        if (stunned) {
            stunTurnsLeft--;

            if (stunTurnsLeft <= 0) {
                stunned = false;
                stunTurnsLeft = 0;
            }
        }
    }

    public void setBoard(Board board) { 
        this.board = board; 
    }
    public Board getBoard() { 
        return board; 
    }

    public int getX() { 
        return x; 
    }

    public int getY() { 
        return y; 
    }

    public void setX(int x) { 
        this.x = x; 
    }

    public void setY(int y) { 
        this.y = y; 
    }

    public int getEnergy() { 
        return energy; 
    }

    public void setEnergy(int e) { 
        this.energy = Math.max(0, e); 
    }

    public int getVisionRange() { 
        return visionRange; 
    }
    public CreatureStats getStats() { 
        return stats; 
    }

    public static class CreatureStats {
        public int maxEnergy;
        public int killCount;
        public int turnsAlive;

        public CreatureStats(int maxEnergy) {
            this.maxEnergy = maxEnergy;
            this.killCount = 0;
            this.turnsAlive = 0;
        }
    }
}
