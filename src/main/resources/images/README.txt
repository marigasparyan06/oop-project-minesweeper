WildHabitat — Expected PNG Assets
==================================

Place PNG files in this directory (src/main/resources/images/).
CreatureRenderer will load them automatically on startup.

NAMING CONVENTION
-----------------
  <creaturekey>_<phase>.png

  creaturekey  = lowercase creature name (no spaces)
  phase        = day | dawn | dusk | night

If a phase variant is missing, the renderer falls back to _day.
If _day is also missing, the renderer falls back to the Canvas silhouette.

EXPECTED FILES (at minimum, provide _day for each creature)
------------------------------------------------------------
  wolf_day.png          wolf_dawn.png         wolf_dusk.png         wolf_night.png
  thornbush_day.png     thornbush_night.png
  nightowl_day.png      nightowl_night.png
  batdefender_day.png   batdefender_night.png
  stoneguard_day.png
  reedwarden_day.png
  rabbit_day.png
  boar_day.png
  nightstalker_night.png
  swampcrawler_day.png

RECOMMENDED SIZE
----------------
  64×64 px  — standard
  128×128 px — 2× / retina (TODO: add retina 2x PNG support)

SUGGESTED SOURCES (CC0 / public domain pixel art)
--------------------------------------------------
  https://opengameart.org       (search "pixel art animals", "pixel art creatures")
  https://itch.io/game-assets   (filter by "Free", "CC0")
  https://kenney.nl/assets      (Kenney's Nature Pack, Animal Pack)

All assets must be CC0, CC-BY, or otherwise freely redistributable.
Include licence notes here if using CC-BY material.
