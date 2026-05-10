import java.util.ArrayList;
import java.util.List;

public class GameController {

    public static final int ENERGY_PER_TURN = 10;
    public static final int SCORE_PER_KILL = 10;
    public static final int SCORE_WAVE_CLEAR = 50; // bonus when wave is cleared
    public static final int ENERGY_WAVE_CLEAR = 20; // energy bonus when wave is cleared
    public static final int NIGHT_ENERGY_DISC = 15; // % discount on defender placement at NIGHT
    public static final int MAX_WAVES = 10; // survive this many waves to win

    private final GameState state;

    private final List<int[]> attackedCells = new ArrayList<>();
    private final List<int[]> movedCells = new ArrayList<>();
    private boolean phaseChangedThisTurn = false;
    private boolean gameOver = false;
    private boolean playerWon = false;

    private int waveCooldown = 0;

    public GameEngine(GameState state) {
        this.state = state;
    }

    public GameState getState() {
        return state;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean didPlayerWin() {
        return playerWon;
    }

    public List<int[]> getLastAttackedCells() {
        return attackedCells;
    }

    public List<int[]> getLastMovedCells() {
        return movedCells;
    }

    public boolean wasPhaseChangedThisTurn() {
        return phaseChangedThisTurn;
    }

    public List<String> nextTurn() {
        if (gameOver)
            return List.of("[GAME] Game is already over.");

        state.messageLog.clear();
        attackedCells.clear();
        movedCells.clear();
        phaseChangedThisTurn = false;

        phaseChangedThisTurn = state.tickDayCycle();
        if (phaseChangedThisTurn) {
            state.log(state.timeOfDay.transitionMessage());
        }

        state.energy += ENERGY_PER_TURN;

        for (Creature c : state.creatures) {
            if (c.isAlive())
                c.tickSlowEffect();
        }

        moveAttackers();
        defendersAttack();
        purgeAndScore();
        handleWaveProgress();

        state.turn++;
        return new ArrayList<>(state.messageLog);
    }

    public String placeDefender(CreatureType type, int row, int col) {
        if (!type.isDefender())
            return "[ERROR] " + type.displayName + " is not a defender.";
        if (!state.inBounds(row, col))
            return "[ERROR] Position out of bounds.";
        if (!state.isCellEmpty(row, col))
            return "[ERROR] Cell (" + row + "," + col + ") is occupied.";

        int cost = effectiveCost(type);
        if (state.energy < cost) {
            return "[ERROR] Not enough energy (" + state.energy + " < " + cost + ").";
        }

        state.energy -= cost;
        state.creatures.add(new Creature(type, row, col));
        return "[PLACE] Placed " + type.displayName + " at (" + row + "," + col
                + ") for " + cost + " energy.";
    }

    public int effectiveCost(CreatureType type) {
        if (!type.isDefender())
            return 0;
        int base = type.energyCost;
        if (state.timeOfDay == TimeOfDay.NIGHT) {
            base = (int) Math.round(base * (1.0 - NIGHT_ENERGY_DISC / 100.0));
        }
        return base;
    }

    private void defendersAttack() {
        List<Creature> defenders = state.livingDefenders();
        for (Creature def : defenders) {
            int range = def.type.attackRange;
            int power = def.getEffectivePower(state.timeOfDay);
            Terrain terrain = state.terrain[def.row][def.col];

            if (def.type == CreatureType.REEDWARDEN && terrain.isWater()) {
                power = (int) (power * 1.2);
            }

            if (def.type == CreatureType.NIGHTOWL && state.timeOfDay == TimeOfDay.DAY) {
                power = (int) (power * 0.4);
            }

            if (def.type == CreatureType.BATDEFENDER && state.timeOfDay == TimeOfDay.DAY) {
                power = (int) (power * 0.15);
            }

            List<Creature> targets = getTargets(def, range);
            if (targets.isEmpty())
                continue;

            switch (def.type) {
                case THORNBUSH:
                case BATDEFENDER:
                case REEDWARDEN:
                    for (Creature t : targets) {
                        dealDamage(def, t, power, terrain);
                    }
                    break;

                case STONEGUARD:
                    Creature strongest = targets.get(0);
                    for (Creature t : targets) {
                        if (t.health > strongest.health)
                            strongest = t;
                    }
                    dealDamage(def, strongest, power * 2, terrain);
                    break;

                case NIGHTOWL:
                    Creature weakest = targets.get(0);
                    for (Creature t : targets) {
                        if (t.health < weakest.health)
                            weakest = t;
                    }
                    dealDamage(def, weakest, power, terrain);
                    break;

                default:
                    break;
            }

        }
    }

    public void triggerNextWave() {
        List<Creature> wave = WaveManager.buildWave(state.wave);
        for (Creature c : wave) {
            int attempts = 0;
            while (state.getCreatureAt(c.row, c.col) != null && attempts < GameState.ROWS) {
                c.row = (c.row + 1) % GameState.ROWS;
                attempts++;
            }
            state.creatures.add(c);
        }
        state.log("[WAVE] Wave " + state.wave + " incoming! (" + wave.size() + " creatures)");
        state.wave++;
        waveCooldown = 0;
    }

    private void moveAttackers() {
        List<Creature> attackers = state.livingAttackers();
        for (Creature a : attackers) {
            if (!a.isAlive())
                continue;
            Terrain t = state.terrain[a.row][a.col];
            int cells = a.computeMoveCells(state.timeOfDay, t);
            if (cells == 0)
                continue;

            for (int step = 0; step < cells; step++) {
                int nextCol = a.col + 1;
                if (nextCol >= GameState.COLS) {
                    state.log("[DEFEAT] " + a.type.displayName + " breached the habitat! Game over.");
                    a.health = 0;
                    gameOver = true;
                    playerWon = false;
                    return;
                }
                a.col = nextCol;
                movedCells.add(new int[] { a.row, a.col });
                
            }
        }
    }

    private void attackersAttack() {
        for (Creature attacker : state.livingAttackers()) {
            Creature target = null;
            for (int dc = 0; dc <= 1; dc++) {
                int checkCol = attacker.col + dc;
                if (!state.inBounds(attacker.row, checkCol))
                    continue;
                Creature c = state.getCreatureAt(attacker.row, checkCol);
                if (c != null && c.isDefender()) {
                    target = c;
                    break;
                }
            }
            if (target == null)
                continue;

            int power = attacker.getAttackerPower(state.timeOfDay);
            target.takeDamage(power);
            attackedCells.add(new int[] { target.row, target.col });
            state.log("[ATTACK] " + attacker.type.abbrev + " → " + target.type.abbrev
                    + " for " + power + " dmg (hp left: " + target.health + ")");
        }
    }
}