package wildhabitat.creatures;

import wildhabitat.enums.TileType;
import wildhabitat.exceptions.InvalidTileException;

public abstract class Attacker extends Creature {

    protected int attackPower;
    protected int speed;
    protected boolean waitingOnStone = false;   //Protected so Bear Can use it in its move()

    public Attacker(int x, int y, int energy, int visionRange, int attackPower, int speed) {
        super(x, y, energy, visionRange);

        this.attackPower = attackPower;
        this.speed = speed;
    }

    public abstract void attack(Defender target);

    protected void moveSteps(int steps) {
        checkAlive();
        if (board == null) {
            return;
        } 

        // Stone penalty: spend one extra turn standing still
        try {
            TileType current = board.getCell(x, y).getTileType();

            if (current == TileType.STONE && !waitingOnStone) {
                waitingOnStone = true;

                return;
            }
        } 
        catch (InvalidTileException e) {
            return;
        }

        waitingOnStone = false;

        for (int step = 0; step < steps; step++) {
            int nextCol = y - 1;

            if (nextCol < 0) {
                return;
            } 

            try {
                var nextCell = board.getCell(x, nextCol);

                if (!nextCell.isEmpty() && nextCell.getOccupant() instanceof Defender def) {
                    attack(def);

                    // Thornbush bounces damage back
                    
                    int reflect = def.getReflectDamage();
                    
                    if (reflect > 0 && isAlive()) {
                        takeDamage(reflect);

                        System.out.println(getClass().getSimpleName()
                                + " took " + reflect + " reflect damage from "
                                + def.getClass().getSimpleName());
                    }

                    return; 
                }

                if (!nextCell.isEmpty()) {
                    return;            // blocked by another attacker
                }  

                if (!canEnter(nextCell.getTileType())) {
                    return; // terrain barrier
                } 

                board.moveCreature(this, x, y, x, nextCol);
                y = nextCol;
            } 
            catch (InvalidTileException e) {
                return;
            }
        }
    }

    public int getAttackPower() { 
        return attackPower; 
    }

    public void setAttackPower(int ap) { 
        this.attackPower = ap; 
    }
    
    public int getSpeed() {
         return speed; 
    }
}
