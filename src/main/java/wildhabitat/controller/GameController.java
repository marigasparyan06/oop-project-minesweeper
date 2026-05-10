public class GameController {

    public static final int ENERGY_PER_TURN   = 10;
    public static final int SCORE_PER_KILL    = 10;
    public static final int SCORE_WAVE_CLEAR  = 50;  // bonus when wave is cleared
    public static final int ENERGY_WAVE_CLEAR = 20;  // energy bonus when wave is cleared
    public static final int NIGHT_ENERGY_DISC = 15;  // % discount on defender placement at NIGHT
    public static final int MAX_WAVES         = 10;  // survive this many waves to win

    private final GameState state;

    private final List<int[]> attackedCells  = new ArrayList<>();
    private final List<int[]> movedCells     = new ArrayList<>();
    private boolean phaseChangedThisTurn     = false;
    private boolean gameOver                 = false;
    private boolean playerWon               = false;

    private int waveCooldown = 0;

    public GameEngine(GameState state) {
        this.state = state;
    }

    public GameState getState() { return state; }

    public boolean isGameOver()  { return gameOver; }
    public boolean didPlayerWin(){ return playerWon; }
    public List<int[]> getLastAttackedCells() { return attackedCells; }
    public List<int[]> getLastMovedCells()    { return movedCells; }
    public boolean wasPhaseChangedThisTurn()  { return phaseChangedThisTurn; }

    public List<String> nextTurn() {
        if (gameOver) return List.of("[GAME] Game is already over.");

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
            if (c.isAlive()) c.tickSlowEffect();
        }

        moveAttackers();
        defendersAttack();
        purgeAndScore();
        handleWaveProgress();

        state.turn++;
        return new ArrayList<>(state.messageLog);
    }