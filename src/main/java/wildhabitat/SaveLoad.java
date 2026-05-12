package main.java.wildhabitat;

import java.io.*;

/**
 * Saves and loads the full game state to/from savegame.txt.
 *
 * Format:
 *   SCORE,<n>
 *   TURN,<n>
 *   WAVE,<n>
 *   ENERGY,<n>
 *   TIME_OF_DAY,<PHASE>
 *   DAY_CYCLE_TICK,<n>
 *   CREATURE,<Type>,<row>,<col>,<hp>
 *   ...
 */
public class SaveLoad {

    public static void save(GameState state, String path) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write("SCORE," + state.getScore()); 
            bw.newLine();
         
            bw.write("TURN," + state.getTurn());   
            bw.newLine();
          
            bw.write("WAVE," + state.getWaveManager().getCurrentWave()); 
            bw.newLine();
          
            bw.write("ENERGY," + state.getEnergy()); 
            bw.newLine();
          
            bw.write("TIME_OF_DAY," + state.getDayCycle().getPhase().name()); 
            bw.newLine();
          
            bw.write("DAY_CYCLE_TICK," + state.getDayCycle().getTicksInPhase()); 
            bw.newLine();
        
            for (Creature c : state.getCreatures()) {
                if (c.isAlive()) {
                    bw.write("CREATURE," + c.getType().name() + "," + c.getRow()
                             + "," + c.getCol() + "," + c.getHp());
                    bw.newLine();
                }
            }
        }
    }

    public static void load(GameState state, String path) throws IOException {
        state.getCreatures().clear();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
         
            String line;
            int wave = 1;
            DayNightCycle.Phase phase = DayNightCycle.Phase.DAY;
            int tick = 0;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                } 

                String[] parts = line.split(",");
                
                switch (parts[0]) {
                    case "SCORE":          
                        state.setScore(Integer.parseInt(parts[1])); 
                        break;
                    case "TURN":           
                        state.setTurn(Integer.parseInt(parts[1])); 
                        break;
                    case "WAVE":           
                        wave = Integer.parseInt(parts[1]); 
                        break;
                    case "ENERGY":         
                        state.setEnergy(Integer.parseInt(parts[1])); 
                        break;
                    case "TIME_OF_DAY":    
                        phase = DayNightCycle.Phase.valueOf(parts[1]); 
                        break;
                    case "DAY_CYCLE_TICK": 
                        tick = Integer.parseInt(parts[1]); 
                        break;
                    case "CREATURE":
                        if (parts.length >= 5) {
                            Creature.Type type = Creature.Type.valueOf(parts[1]);
                            int row = Integer.parseInt(parts[2]);
                            int col = Integer.parseInt(parts[3]);
                            int hp  = Integer.parseInt(parts[4]);

                            state.getCreatures().add(new Creature(type, row, col, hp));
                        }

                        break;
                }
            }
            
            state.setDayCycle(new DayNightCycle(phase, tick));
            state.setWaveManager(new WaveManager(wave));
        }
    }
}

