package wildhabitat.interfaces;

import wildhabitat.creatures.Creature;

public interface Shootable {
    void shoot(Creature target);
    int getRange();
}
