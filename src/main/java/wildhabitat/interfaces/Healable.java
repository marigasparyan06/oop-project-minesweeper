package wildhabitat.interfaces;

import wildhabitat.creatures.Creature;

public interface Healable {
    void heal(Creature target);
    int getHealPower();
}
