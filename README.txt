WildHabitat
===========
A wave-defence game set in a wildlife habitat.
Runs in two modes: a plain terminal (CLI) or a JavaFX graphical UI.

Prerequisites
-------------
- JDK 17 or newer
  Download: https://adoptium.net  or  https://www.oracle.com/java/technologies/downloads/
  Verify:   java -version

- JavaFX SDK 21 or newer  (UI mode only — CLI needs no JavaFX)
  Download: https://gluonhq.com/products/javafx/
  Pick the SDK zip for your OS, unzip it somewhere convenient.


Setup — macOS / Linux
---------------------
1. Install JDK 17+ and confirm:
     java -version
     javac -version

2. Download and unzip JavaFX SDK (UI only).
   Example unzip location: /opt/javafx-sdk-21/

3. Set JAVAFX_PATH (UI only):
     export JAVAFX_PATH=/opt/javafx-sdk-21/lib
   Add this line to ~/.bashrc or ~/.zshrc to make it permanent.

4. From the project root directory:

   CLI mode (no JavaFX needed):
     chmod +x run_cli.sh
     ./run_cli.sh

   UI mode:
     chmod +x run_ui.sh
     ./run_ui.sh


Setup — Windows
---------------
1. Install JDK 17+ and confirm in Command Prompt:
     java -version
     javac -version

2. Download and unzip JavaFX SDK (UI only).
   Example unzip location: C:\javafx-sdk-21\

3. Set JAVAFX_PATH (UI only) — in the same Command Prompt session:
     set JAVAFX_PATH=C:\javafx-sdk-21\lib
   Or set it permanently via System Properties > Environment Variables.

4. From the project root directory:

   CLI mode (no JavaFX needed):
     run_cli.bat

   UI mode:
     run_ui.bat


Compiling manually (without the scripts)
-----------------------------------------
CLI:
  mkdir out\cli
  javac -d out\cli src\cli\*.java src\core\*.java
  java -cp out\cli CLIGame

UI (replace path with your actual JavaFX SDK location):
  mkdir out\ui
  javac --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.graphics ^
        -d out\ui src\cli\*.java src\core\*.java src\ui\*.java
  java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.graphics ^
       -cp out\ui GameUI

Debug mode (enables the "Skip Phase" button in the UI):
  java -Ddebug=true --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.graphics ^
       -cp out\ui GameUI


CLI commands
------------
  place <type> <row> <col>   Place a defender (e.g. place Thornbush 3 5)
  next                       Advance one turn
  wave                       Start the next wave
  save                       Save game to savegame.txt
  load                       Load game from savegame.txt
  log                        Print recent event log
  defenders                  List defender types and costs
  quit                       Exit

Defender types: Thornbush, NightOwl, BatDefender, StoneGuard, ReedWarden


UI controls
-----------
  Left panel   — click a defender type to select it, then click a grid cell to place it
  Next Turn    — advance the simulation one turn
  Start/Pause  — pause automatic turn processing
  Save / Load  — save or restore from savegame.txt
  Load Wave    — manually trigger the next attacker wave


PNG image assets
----------------
Drop creature PNGs into:  resources/images/
See resources/images/README.txt for the full list of expected filenames,
recommended dimensions (64x64 or 128x128), and free asset sources.

The renderer falls back gracefully:
  missing phase variant  ->  uses <creature>_day.png
  missing _day.png       ->  draws a canvas silhouette
  any render error       ->  shows an emoji (e.g. Wolf=🐺, Thornbush=🌿)


Project layout
--------------
  src/core/    Shared game logic — no JavaFX, plain JDK only
  src/cli/     Terminal interface — no JavaFX, compiles without JavaFX SDK
  src/ui/      JavaFX graphical interface — requires JavaFX SDK
  resources/   Images and other assets
  gridstate.txt   Terrain map (loaded on startup, saved on request)
  savegame.txt    Game save (score, turn, creatures, day/night state)


Day/Night cycle
---------------
Every 6 turns cycles through: DAWN -> DAY -> DUSK -> NIGHT (3 turns each)

  DAY   Baseline stats. Full terrain colours.
  DAWN  Attackers 75% speed. Defenders +10% power. Warm-orange tint.
  DUSK  Attackers +10% speed. Amber tint.
  NIGHT Attackers +25% speed, +15% attack. Defenders -10% effectiveness
        (NightOwl and BatDefender unaffected). Placement costs -15% energy.
        NightStalker is frozen during DAY.


Win / Lose
----------
  Win:  Survive all 10 waves.
  Lose: Any attacker reaches the right edge of the grid.
