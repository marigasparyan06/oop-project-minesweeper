package com.wildhabitat.renderer;

import com.wildhabitat.model.Creature;
import com.wildhabitat.model.CreatureType;
import com.wildhabitat.model.TimeOfDay;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.HashMap;

/**
 * Renders creatures using PNG images from resources/images/.
 * If no image is found for a creature, a colored oval placeholder is drawn instead.
 * Expected file naming: {creature}_{phase}.png (e.g. wolf_day.png, reedwarden_night.png).
 */
public class CreatureRenderer {

    private static final HashMap<String, Image> imageCache = new HashMap<>();

    static {
        loadAllImages();
    }

    private static void loadAllImages() {
        String[] types  = {"wolf","thornbush","nightowl","batdefender",
                            "stoneguard","reedwarden","rabbit","boar",
                            "nightstalker","swampcrawler"};
        String[] phases = {"day","dawn","dusk","night"};

        for (String type : types) {
            for (String phase : phases) {
                String key  = type + "_" + phase;
                String path = "/images/" + key + ".png";
                try {
                    InputStream is = CreatureRenderer.class.getResourceAsStream(path);
                    if (is != null) {
                        imageCache.put(key, new Image(is, 128, 128, true, true));
                        is.close();
                    }
                } catch (Exception e) {
                    System.out.println("[Renderer] No image for: " + key);
                }
            }
        }
    }

    /**
     * Draws the creature into the given cell area.
     * Tries the phase-specific PNG first, then the day PNG, then a colored oval placeholder.
     */
    public static void render(GraphicsContext gc, Creature creature, TimeOfDay phase,
                               double cellX, double cellY, double cellSize) {
        String typeName = creature.type.name().toLowerCase();
        String phaseKey = typeName + "_" + phase.name().toLowerCase();

        Image img = imageCache.get(phaseKey);
        if (img == null) {
            img = imageCache.get(typeName + "_day");
        }

        double pad = (cellSize - 52) / 2.0;

        if (img != null) {
            gc.drawImage(img, cellX + pad, cellY + pad, 52, 52);
            applyNightGlowOverlay(gc, creature.type, phase, cellX, cellY, cellSize);
        } else {
            gc.setFill(Color.web(creature.type.colorHex));
            gc.fillOval(cellX + pad, cellY + pad, 52, 52);
        }
    }

    private static void applyNightGlowOverlay(GraphicsContext gc, CreatureType type,
                                               TimeOfDay phase, double cx, double cy, double size) {
        if (phase == TimeOfDay.DAY) {
            if (type == CreatureType.NIGHTOWL) {
                gc.setGlobalAlpha(0.4);
                gc.setFill(Color.web("#000000", 0.6));
                gc.fillRect(cx, cy, size, size);
                gc.setGlobalAlpha(1.0);
            } else if (type == CreatureType.BATDEFENDER) {
                gc.setGlobalAlpha(0.15);
                gc.setFill(Color.web("#000000", 0.85));
                gc.fillRect(cx, cy, size, size);
                gc.setGlobalAlpha(1.0);
            }
        }
        if (phase != TimeOfDay.NIGHT) return;

        String glowHex;
        switch (type) {
            case WOLF:         glowHex = "#ffe082"; break;
            case NIGHTOWL:     glowHex = "#ce93d8"; break;
            case BATDEFENDER:  glowHex = "#64b5f6"; break;
            case NIGHTSTALKER: glowHex = "#ef5350"; break;
            default: return;
        }

        gc.setFill(Color.web(glowHex, 0.18));
        gc.fillOval(cx + 4, cy + 4, size - 8, size - 8);
    }

    /**
     * Draws a health bar at the bottom of the cell. Color shifts green → orange → red as health drops.
     */
    public static void drawHPBar(GraphicsContext gc, Creature c,
                                  double cellX, double cellY, double cellW) {
        double barY = cellY + cellW - 8;
        double barW = cellW - 8;
        double barX = cellX + 4;
        double fill = barW * ((double) c.health / c.maxHealth);

        gc.setFill(Color.web("#222222", 0.7));
        gc.fillRect(barX, barY, barW, 5);

        double ratio = (double) c.health / c.maxHealth;
        Color barColor;
        if (ratio > 0.6)      barColor = Color.web("#66bb6a");
        else if (ratio > 0.3) barColor = Color.web("#ffa726");
        else                   barColor = Color.web("#ef5350");

        gc.setFill(barColor);
        gc.fillRect(barX, barY, fill, 5);
    }
}
