WildHabitat

A tower defense game built with Java 17 and JavaFX 21
The player places defenders on a grid to stop waves of attackers from crossing to the right side
The game has a day and night cycle that changes creature speed and power every few turns


How to run

    mvn clean javafx:run


How to play

Select a defender from the left panel then click a grid cell to place it
Click Start to begin automatic turn processing
Click a cell with no defender selected to see creature info in the log
Use Save and Load to persist your game
Use Load Wave to manually trigger the next wave


The grid

7 rows and 11 columns
Attackers spawn on the left side and march right
If any attacker reaches the right edge the game is over
Rows 2 and 5 are water terrain
Rows 0 1 3 4 6 are land terrain


Creatures

There are 5 attacker types and 5 defender types
Each creature can only be placed on or travel through certain terrain types
Water creatures like Attacker5 and Defender5 only work on water rows
Land creatures like Attacker2 and Defender1 only work on land rows
Attacker4 and Defender3 can access all terrain


Day and night cycle

The phase changes every 3 turns cycling through dawn day dusk and night
At dawn attackers slow to 75 percent speed and defenders gain 10 percent power
At dusk attackers gain 10 percent speed
At night attackers gain 25 percent speed and 15 percent attack power
At night defender placement costs 15 percent less energy
Attacker4 is frozen and cannot move during the day
Defender2 and Defender3 are night specialists and gain 25 percent power at night


Defender behavior

Defender1 hits all enemies in range and applies a slow effect
Defender2 targets the weakest enemy in range
Defender3 hits all enemies in range
Defender4 targets the strongest enemy in range with double damage
Defender5 hits all enemies in range and deals 20 percent more damage on water


Win and lose

Survive all 10 waves to win
If any attacker reaches the right edge of the grid the game ends


Files

gridstate.txt    terrain layout loaded on startup
savegame.txt     saved game state including score wave and all creature positions
src/main/java    all source code
src/main/resources/images    creature PNG images named attacker1.png through defender5.png
