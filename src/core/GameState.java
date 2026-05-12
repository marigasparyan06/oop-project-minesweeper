package main.java.wildhabitat;

import java.util.*;
/**
 * Central game state. Holds grid, creatures, turn counter, score, energy.
 * Exposes processTurn() which advances simulation one step.
 */
public class GameState {
    private Grid grid;
    private List<Creature> creatures;
    private int turn;
    private int score;
    private int energy;
    private DayNightCycle dayCycle;
    private WaveManager waveManager;
    private List<String> messageLog;
    private boolean gameOver;
    private boolean playerWon;

    // Attacker spawn column is 0, they march right toward column COLS-1
    // Defenders placed anywhere by player, they fight adjacent attackers

    public GameState() {
        grid = new Grid();
        creatures = new ArrayList<>();
        turn = 1;
        score = 0;
        energy = 100;
        dayCycle = new DayNightCycle();
        waveManager = new WaveManager();
        messageLog = new ArrayList<>();
        gameOver = false;
        playerWon = false;
    }

    //  Accessors 

    public Grid getGrid() { 
        return grid; 
    }

    public List<Creature> getCreatures() { 
        return creatures; 
    }

    public int getTurn() { 
        return turn; 
    }

    public int getScore() { 
        return score; 
    }

    public int getEnergy() { 
        return energy; 
    }

    public DayNightCycle getDayCycle() { 
        return dayCycle; 
    }

    public WaveManager getWaveManager() { 
        return waveManager; 
    }

    public List<String> getMessageLog() { 
        return messageLog; 
    }

    public boolean isGameOver() { 
        return gameOver; 
    }

    public boolean isPlayerWon() { 
        return playerWon; 
    }

    public void setGrid(Grid g) { 
        grid = g; 
    }

    public void setTurn(int t) { 
        turn = t; 
    }

    public void setScore(int s) { 
        score = s; 
    }

    public void setEnergy(int e) { 
        energy = e; 
    }

    public void setDayCycle(DayNightCycle d) { 
        dayCycle = d; 
    }
    
    public void setWaveManager(WaveManager w) { 
        waveManager = w; 
    }

    //  Player actions 

    /**
     * Place a defender if the player has enough energy and the cell is empty.
     * Returns a message describing success or failure.
     */
    public String placeDefender(Creature.Type type, int row, int col) {
        if (!grid.inBounds(row, col)) {
            return "Invalid position.";
        }

        if (getCreatureAt(row, col) != null) {
            return "Cell occupied.";
        }

        Creature defender = new Creature(type, row, col);
        int cost = defender.getPlacementCost();

        // Night gives -15% placement cost
        if (dayCycle.getPhase() == DayNightCycle.Phase.NIGHT) {
            cost = (int)(cost * 0.85);
        }

        if (energy < cost) {
            return "Not enough energy (need " + cost + ", have " + energy + ").";
        }

        energy -= cost;
        creatures.add(defender);
     
        log("[PLACE] Placed " + type.name() + " at (" + row + "," + col + ") for " + cost + " energy.");
       
        return "Placed " + type.name() + ".";
    }

    //  Wave management 

    /** Start the next wave, spawning attackers. */
    public void startNextWave() {
        List<Creature> attackers = waveManager.spawnWave(dayCycle.getPhase());
        creatures.addAll(attackers);
        log("[WAVE] Wave " + (waveManager.getCurrentWave() - 1) + " begins! " + attackers.size() + " attackers incoming.");
    }

    //  Turn processing 

    /**
     * Advance the simulation by one turn.
     * Order: attackers move, combat resolves, dead creatures removed,
     *        energy trickle, day/night tick, wave completion check.
     */
    public void processTurn() {
        DayNightCycle.Phase phase = dayCycle.getPhase();

        // Move attackers left→right
        for (Creature c : creatures) {
            if (!c.isAlive() || c.getRole() != Creature.Role.ATTACKER) {
                continue;
            } 

            double speed = c.getSpeedMultiplier(phase);
            if (speed <= 0) {
                continue; 
            } 

            // Speed > 1.0 means faster, we use a probabilistic step this turn
            // Base: move every turn. Speed 0.75 means 75% chance per turn.
         
            double roll = Math.random();
            if (roll >= speed && speed < 1.0) {
                continue; // skip move
            } 

            int nextCol = c.getCol() + 1;

            if (!grid.inBounds(c.getRow(), nextCol)) {
                // Attacker reached the right edge — game over
                
                gameOver = true;
                playerWon = false;
                
                log("[DEFEAT] " + c.getType().name() + " breached the right boundary! Game over.");
                
                return;
            }

            Creature occupant = getCreatureAt(c.getRow(), nextCol);
          
            if (occupant == null) {
                c.setCol(nextCol);
                log("[MOVE] " + c.getType().name() + " moves to (" + c.getRow() + "," + nextCol + ").");
            }
            // If occupied by a defender, they'll fight in the combat phase
        }

        // Combat: each attacker attacks adjacent defenders, each defender attacks adjacent attackers
        List<Creature> alive = getLiving();
        
        for (Creature attacker : alive) {
            if (attacker.getRole() != Creature.Role.ATTACKER) {
                continue;
            } 

            Creature target = getAdjacentDefender(attacker);
            
            if (target != null) {
                int dmg = attacker.getEffectiveAttack(phase);
                boolean killed = target.takeDamage(dmg);
              
                log("[ATTACK] " + attacker.getType().name() + " attacks " + target.getType().name()
                    + " for " + dmg + " dmg." + (killed ? " DEFEATED!" : ""));
                if (killed) {
                    score += 10;
                } 
            }
        }
        for (Creature defender : alive) {
            if (defender.getRole() != Creature.Role.DEFENDER) {
                continue;
            } 

            Creature target = getAdjacentAttacker(defender);
            
            if (target != null) {
                int dmg = computeDefenderDamage(defender, target, phase);
                boolean killed = target.takeDamage(dmg);
            
                log("[DEFEND] " + defender.getType().name() + " defends against " + target.getType().name()
                    + " for " + dmg + " dmg." + (killed ? " KILLED!" : ""));
                if (killed) { 
                    score += 15; 
                    energy += 5; 
                }
            }
        }

        // Remove dead creatures
        creatures.removeIf(c -> !c.isAlive());

        // Energy trickle
        energy += 10;

        // Advance day/night cycle
        boolean phaseChanged = dayCycle.tick();
        if (phaseChanged) {
            log(dayCycle.getTransitionMessage());
        }

        // Wave completion
        if (waveManager.isWaveActive() && waveManager.isWaveComplete(creatures)) {
            waveManager.setWaveActive(false);

            score += 50;
            energy += 20;

            log("[WAVE] Wave cleared! +50 score, +20 energy.");
        }

        // Win condition: survived 10 waves
        if (waveManager.getCurrentWave() > 10 && !waveManager.isWaveActive()) {
            gameOver = true;
            playerWon = true;

            log("[WIN] All waves cleared! Final score: " + score);
        }

        ++turn;
    }

    //  Helpers 

    public Creature getCreatureAt(int row, int col) {
        for (Creature c : creatures) {
            if (c.isAlive() && c.getRow() == row && c.getCol() == col) {
                return c;
            }
        }

        return null;
    }

    private List<Creature> getLiving() {
        List<Creature> list = new ArrayList<>();
        for (Creature c : creatures) {
            if (c.isAlive()) {
                list.add(c);
            } 
        }

        return list;
    }

    // Attacker attacks the defender in the cell directly to its right
    private Creature getAdjacentDefender(Creature attacker) {
        int r = attacker.getRow(), c = attacker.getCol();
        Creature right = getCreatureAt(r, c + 1);

        if (right != null && right.getRole() == Creature.Role.DEFENDER) {
            return right;
        }   

        Creature same = getCreatureAt(r, c);
        
        if (same != null && same != attacker && same.getRole() == Creature.Role.DEFENDER) {
            return same;
        } 

        return null;
    }

    // Defender attacks the nearest attacker in the same row to its left
    private Creature getAdjacentAttacker(Creature defender) {
        int r = defender.getRow(), c = defender.getCol();

        // Check left cell
        Creature left = getCreatureAt(r, c - 1);
        
        if (left != null && left.getRole() == Creature.Role.ATTACKER) {
            return left;
        } 

        // Check same cell (attacker may have walked into defender's cell)
        
        Creature same = getCreatureAt(r, c);
        
        if (same != null && same != defender && same.getRole() == Creature.Role.ATTACKER) {
            return same;
        } 

        return null;
    }

    private int computeDefenderDamage(Creature defender, Creature attacker, DayNightCycle.Phase phase) {
        int dmg = defender.getEffectiveAttack(phase);
        // ReedWarden gets +20% on water terrain

        if (defender.getType() == Creature.Type.ReedWarden) {
            Grid.Terrain t = grid.get(defender.getRow(), defender.getCol());
            if (t == Grid.Terrain.SHALLOW_WATER || t == Grid.Terrain.DEEP_WATER) {
                dmg = (int)(dmg * 1.2);
            }
        }

        // Thornbush: flash effect is UI only; damage is same
        return dmg;
    }

    private void log(String msg) {
        messageLog.add(msg);
        
        if (messageLog.size() > 200) {
            messageLog.remove(0);
        } 
    }
}
