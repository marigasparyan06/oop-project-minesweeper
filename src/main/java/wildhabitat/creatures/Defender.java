package wildhabitat.creatures;

public abstract class Defender extends Creature {

    protected int defense;
    protected int cost;

    public Defender(int x, int y, int energy, int visionRange, int defense, int cost) {
        super(x, y, energy, visionRange);

        this.defense = defense;
        this.cost = cost;
    }

    public abstract int defend(int incomingDamage);

    public int getReflectDamage() { return 0; }

    @Override
    public void move() {
        // Most defenders are stationary; subclasses may override
    }

    public int getDefense() { 
        return defense; 
    }

    public int getCost() { 
        return cost; 
    }
}
