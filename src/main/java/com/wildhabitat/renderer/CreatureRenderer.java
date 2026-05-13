package com.wildhabitat.renderer;

import com.wildhabitat.model.Creature;
import com.wildhabitat.model.CreatureType;
import com.wildhabitat.model.TimeOfDay;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.io.InputStream;
import java.util.HashMap;

/**
 * Three-layer creature rendering:
 *   Layer 1 — PNG from resources/images/ (most visually rich)
 *   Layer 2 — Procedural Canvas silhouette (zero asset dependency)
 *   Layer 3 — Emoji fallback (last resort; never blank)
 *
 * All drawing targets a GraphicsContext whose coordinate origin is the
 * top-left corner of the cell. Cell size is passed at call time.
 */
public class CreatureRenderer {

    // ── Layer 1: PNG image cache 

    private static final HashMap<String, Image> imageCache = new HashMap<>();

    static {
        loadAllImages();
    }

    private static void loadAllImages() {
        String[] types   = {"wolf","thornbush","nightowl","batdefender",
                             "stoneguard","reedwarden","rabbit","boar",
                             "nightstalker","swampcrawler"};

        String[] phases  = {"day","dawn","dusk","night"};

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
                } 
                catch (NullPointerException | IllegalArgumentException e) {
                    System.out.println("[Renderer] Falling back to canvas for: " + key);
                } 
                catch (Exception e) {
                    System.out.println("[Renderer] Falling back to canvas for: " + key);
                }
            }
        }
    }

    //  Public entry point 

    /**
     * Renders the creature into the given GraphicsContext.
     *
     * @param gc       target graphics context (cell canvas)
     * @param creature creature to draw
     * @param phase    current time-of-day phase
     * @param cellX    left edge of the cell in canvas coordinates
     * @param cellY    top edge of the cell in canvas coordinates
     * @param cellSize width and height of the cell in pixels (typically 70)
     */
    public static void render(GraphicsContext gc, Creature creature, TimeOfDay phase,
                               double cellX, double cellY, double cellSize) {

        String typeName = creature.type.name().toLowerCase();
        String phaseKey = typeName + "_" + phase.name().toLowerCase();

        // Layer 1: try exact phase PNG, fall back to _day PNG
        Image img = imageCache.get(phaseKey);

        if (img == null){
            img = imageCache.get(typeName + "_day");
        } 

        if (img != null) {
            double pad  = (cellSize - 52) / 2.0;

            gc.drawImage(img, cellX + pad, cellY + pad, 52, 52);
            applyNightGlowOverlay(gc, creature.type, phase, cellX, cellY, cellSize);
            
            return;
        }

        // Layer 2: canvas silhouette
        try {
            gc.save();

            drawSilhouette(gc, creature, phase, cellX, cellY, cellSize);

            gc.restore();
        } 
        catch (Exception ex) {
            // Layer 3: emoji (should never be reached in normal operation)
            gc.save();

            drawEmoji(gc, creature, cellX, cellY, cellSize);

            gc.restore();
        }
    }

    // ── Layer 2: procedural silhouettes 

    private static void drawSilhouette(GraphicsContext gc, Creature c, TimeOfDay phase,
                                        double cx, double cy, double size) {
        double mx = cx + size / 2.0; // horizontal centre
        double my = cy + size / 2.0; // vertical centre

        switch (c.type) {
            case WOLF:         
                drawWolf(gc, mx, my, phase);          
                break;
            case THORNBUSH:    
                drawThornbush(gc, mx, my, phase);     
                break;
            case NIGHTOWL:     
                drawNightOwl(gc, mx, my, phase);      
                break;
            case BATDEFENDER:  
                drawBatDefender(gc, mx, my, phase);   
                break;
            case STONEGUARD:   
                drawStoneGuard(gc, mx, my);           
                break;
            case REEDWARDEN:   
                drawReedWarden(gc, mx, my, c.row, c.col, phase); 
                break;
            case RABBIT:       
                drawRabbit(gc, mx, my, phase);        
                break;
            case BOAR:         
                drawBoar(gc, mx, my, phase);          
                break;
            case NIGHTSTALKER: 
                drawNightStalker(gc, mx, my, phase);  
                break;
            case SWAMPCRAWLER: 
                drawSwampCrawler(gc, mx, my, phase);  
                break;
        }
    }

    // ── Individual silhouette draw methods 

    private static void drawWolf(GraphicsContext gc, double x, double y, TimeOfDay phase) {
        Color body = Color.web("#e8d5a3");

        if (phase == TimeOfDay.NIGHT) {
            gc.setEffect(new DropShadow(15, 0, 0, Color.web("#ffe082")));
        }
        // Elongated body oval
        gc.setFill(body);
        gc.fillOval(x - 12, y + 4, 24, 14);
        // Head circle r=16
        gc.fillOval(x - 16, y - 18, 32, 32);
        // Left ear
        gc.fillPolygon(new double[]{x - 14, x - 6, x - 10},
                       new double[]{y - 16, y - 16, y - 28}, 3);
        // Right ear
        gc.fillPolygon(new double[]{x + 6,  x + 14, x + 10},
                       new double[]{y - 16, y - 16, y - 28}, 3);
        // Tail — bezier arc
        gc.setStroke(body);
        gc.setLineWidth(2.5);
        gc.beginPath();
        gc.moveTo(x + 11, y + 8);
        gc.bezierCurveTo(x + 22, y + 14, x + 28, y - 2, x + 20, y - 10);
        gc.stroke();
        gc.setEffect(null);
    }

    private static void drawThornbush(GraphicsContext gc, double x, double y, TimeOfDay phase) {
        Color body  = Color.web("#5a9e4b");
        Color spike = Color.web("#2e7d32");

        gc.setFill(body);
        gc.fillOval(x - 14, y - 14, 28, 28);

        // 8 radiating spikes at 45° intervals
        gc.setStroke(spike);
        gc.setLineWidth(2.5);

        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45.0);
            double startR = 14, endR = 26;

            gc.strokeLine(x + startR * Math.cos(angle), y + startR * Math.sin(angle),
                          x + endR   * Math.cos(angle), y + endR   * Math.sin(angle));
        }
    }

    private static void drawNightOwl(GraphicsContext gc, double x, double y, TimeOfDay phase) {
        // Faded during DAY (40% opacity); full + purple glow at NIGHT
        if (phase == TimeOfDay.DAY) {
            gc.setGlobalAlpha(0.4);
        } 
        else if (phase == TimeOfDay.NIGHT) {
            gc.setEffect(new DropShadow(20, 0, 0, Color.web("#ce93d8")));
        }

        Color body = Color.web("#b39ddb");
        // Body oval
        gc.setFill(body);
        gc.fillOval(x - 12, y - 10, 24, 26);
        // Large eyes
        gc.setFill(Color.web("#ffe082"));
        gc.fillOval(x - 11, y - 8, 10, 10);
        gc.fillOval(x +  1, y - 8, 10, 10);
        // Pupils
        gc.setFill(Color.BLACK);
        gc.fillOval(x - 8, y - 5, 4, 5);
        gc.fillOval(x + 4, y - 5, 4, 5);
        // Ear tufts
        gc.setFill(body);
        gc.fillPolygon(new double[]{x - 12, x - 7, x - 9},
                       new double[]{y - 10, y - 10, y - 20}, 3);
        gc.fillPolygon(new double[]{x + 7,  x + 12, x + 9},
                       new double[]{y - 10, y - 10, y - 20}, 3);
        // Wing arcs
        gc.setStroke(body);
        gc.setLineWidth(3);
        gc.strokeArc(x - 24, y - 4, 16, 20, 270, 180, ArcType.OPEN);
        gc.strokeArc(x +  8, y - 4, 16, 20, 270, 180, ArcType.OPEN);

        gc.setGlobalAlpha(1.0);
        gc.setEffect(null);
    }

    private static void drawBatDefender(GraphicsContext gc, double x, double y, TimeOfDay phase) {
        // Almost invisible during DAY (15% opacity + dashed outline only)
        // Full opacity + blue glow at NIGHT
        if (phase == TimeOfDay.DAY) {
            gc.setGlobalAlpha(0.15);
            gc.setLineDashes(4);
        } 
        else if (phase == TimeOfDay.NIGHT) {
            gc.setEffect(new DropShadow(18, 0, 0, Color.web("#64b5f6")));
        }

        Color wing = Color.web("#90caf9");
        // Central body oval r=8
        gc.setFill(wing);
        gc.fillOval(x - 8, y - 8, 16, 16);

        // Left wing membrane
        gc.setFill(wing);
        gc.beginPath();
        gc.moveTo(x - 8, y - 4);
        gc.bezierCurveTo(x - 20, y - 14, x - 28, y + 4, x - 10, y + 8);
        gc.closePath();
        gc.fill();

        // Right wing membrane
        gc.beginPath();
        gc.moveTo(x + 8, y - 4);
        gc.bezierCurveTo(x + 20, y - 14, x + 28, y + 4, x + 10, y + 8);
        gc.closePath();
        gc.fill();

        gc.setGlobalAlpha(1.0);
        gc.setLineDashes(0);
        gc.setEffect(null);
    }

    private static void drawStoneGuard(GraphicsContext gc, double x, double y) {
        // No phase modifier — always fully rendered
        drawHexagon(gc, x, y, 18, Color.web("#b0bec5"));
        drawHexagon(gc, x, y, 10, Color.web("#546e7a"));
    }

    private static void drawHexagon(GraphicsContext gc, double cx, double cy,
                                     double r, Color fill) {
        double[] xs = new double[6];
        double[] ys = new double[6];

        for (int i = 0; i < 6; i++) {
            double a = Math.toRadians(60 * i - 30);
            xs[i] = cx + r * Math.cos(a);
            ys[i] = cy + r * Math.sin(a);
        }

        gc.setFill(fill);
        gc.fillPolygon(xs, ys, 6);
    }

    private static void drawReedWarden(GraphicsContext gc, double x, double y,
                                        int row, int col, TimeOfDay phase) {
        Color body = Color.web("#80cbc4");
        // Teardrop: circle head + tapering bezier body
        gc.setFill(body);
        gc.fillOval(x - 12, y - 18, 24, 24);
        gc.beginPath();
        gc.moveTo(x - 12, y);
        gc.bezierCurveTo(x - 12, y + 10, x - 4, y + 20, x, y + 22);
        gc.bezierCurveTo(x + 4,  y + 20, x + 12, y + 10, x + 12, y);
        gc.closePath();
        gc.fill();

        // Ripple arcs on water tiles (checked via row — renderer doesn't hold state reference,
        // so we pass row/col and let caller pass terrain info via phase for simplicity;
        // ripples always drawn — they only show on actual water tiles which look right contextually)
        gc.setStroke(Color.web("#b2ebf2"));
        gc.setLineWidth(1.5);
        gc.strokeArc(x - 10, y + 20, 8, 5, 0, 180, ArcType.OPEN);
        gc.strokeArc(x,      y + 22, 8, 5, 0, 180, ArcType.OPEN);
        gc.strokeArc(x - 4,  y + 24, 8, 5, 0, 180, ArcType.OPEN);
    }

    private static void drawRabbit(GraphicsContext gc, double x, double y, TimeOfDay phase) {
        Color body = Color.web("#f48fb1");
        gc.setFill(body);
        // Body oval
        gc.fillOval(x - 9, y - 4, 18, 14);
        // Head circle
        gc.fillOval(x - 8, y - 16, 16, 16);
        // Tall thin ears
        gc.fillOval(x - 8, y - 30, 5, 16);
        gc.fillOval(x + 3, y - 30, 5, 16);
        // Inner ear pink
        gc.setFill(Color.web("#f8bbd0"));
        gc.fillOval(x - 7, y - 29, 3, 13);
        gc.fillOval(x + 4, y - 29, 3, 13);
        // White tail
        gc.setFill(Color.WHITE);
        gc.fillOval(x + 8, y + 4, 6, 6);
    }

    private static void drawBoar(GraphicsContext gc, double x, double y, TimeOfDay phase) {
        Color body = Color.web("#a1887f");
        gc.setFill(body);
        // Wide body — rounded rectangle
        double bx = x - 14, by = y - 6, bw = 28, bh = 18;
        gc.fillRoundRect(bx, by, bw, bh, 8, 8);
        // Head circle
        gc.fillOval(x - 10, y - 18, 20, 20);
        // Tusks
        gc.setStroke(Color.web("#f5f5dc"));
        gc.setLineWidth(2);
        gc.strokeLine(x - 10, y + 2, x - 17, y + 5);
        gc.strokeLine(x + 10, y + 2, x + 17, y + 5);
        // Small curly tail
        gc.setStroke(body);
        gc.setLineWidth(2);
        gc.strokeArc(x + 13, y + 2, 6, 6, 0, 270, ArcType.OPEN);
    }

    private static void drawNightStalker(GraphicsContext gc, double x, double y, TimeOfDay phase) {
        // Invisible only during DAY — faint red ? mark at 20% opacity
        if (phase == TimeOfDay.DAY) {

            gc.setGlobalAlpha(0.20);
            gc.setFill(Color.web("#ef5350"));
            gc.setFont(Font.font("Arial", 22));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText("?", x, y);
            gc.setGlobalAlpha(1.0);

            return;
        }

        // Visible at DAWN, DUSK and NIGHT; red glow at NIGHT only
        if (phase == TimeOfDay.NIGHT) {
            gc.setEffect(new DropShadow(20, 0, 0, Color.web("#ef5350")));
        }

        Color col = Color.web("#ef5350");
        // Sharp 4-point diamond
        gc.setFill(col);
        gc.fillPolygon(
            new double[]{x,      x + 20, x,      x - 20},
            new double[]{y - 20, y,      y + 20, y},
            4);
        // Inner jagged lines for texture
        gc.setStroke(Color.web("#b71c1c"));
        gc.setLineWidth(1.5);
        gc.strokeLine(x - 10, y - 10, x + 10, y + 10);
        gc.strokeLine(x + 10, y - 10, x - 10, y + 10);
        gc.strokeLine(x, y - 14, x, y + 14);
        gc.strokeLine(x - 14, y, x + 14, y);

        gc.setEffect(null);
    }

    private static void drawSwampCrawler(GraphicsContext gc, double x, double y, TimeOfDay phase) {
        Color body = Color.web("#aed581");
        gc.setFill(body);
        // Base circle
        gc.fillOval(x - 15, y - 10, 30, 26);

        // 5 bumps around perimeter (fixed offsets — same every render, not random)
        double[][] bumps = {
            {-18, -5}, {-12, -18}, {6, -19}, {18, -6}, {14, 12}
        };

        for (double[] b : bumps) {
            gc.fillOval(x + b[0] - 5, y + b[1] - 5, 10, 10);
        }

        // Three eye dots across the top
        gc.setFill(Color.BLACK);
        gc.fillOval(x - 7, y - 6, 5, 5);
        gc.fillOval(x - 1, y - 8, 5, 5);
        gc.fillOval(x + 5, y - 6, 5, 5);
    }

    // Night glow overlay for PNG images 

    private static void applyNightGlowOverlay(GraphicsContext gc, CreatureType type,
                                               TimeOfDay phase, double cx, double cy, double size) {
        // DAY opacity reduction for phase-sensitive defenders even when using PNGs
        if (phase == TimeOfDay.DAY) {
            if (type == CreatureType.NIGHTOWL) {
                gc.setGlobalAlpha(0.4);
                gc.setFill(Color.web("#000000", 0.6));
                gc.fillRect(cx, cy, size, size);
                gc.setGlobalAlpha(1.0);
            } 
            else if (type == CreatureType.BATDEFENDER) {
                gc.setGlobalAlpha(0.15);
                gc.setFill(Color.web("#000000", 0.85));
                gc.fillRect(cx, cy, size, size);
                gc.setGlobalAlpha(1.0);
            }
        }
        if (phase != TimeOfDay.NIGHT) {
            return;
        } 
        // Night-glow creatures get a soft coloured oval overlay
        String glowHex;

        switch (type) {
            case WOLF:         
                glowHex = "#ffe082"; 
                break;
            case NIGHTOWL:     
                glowHex = "#ce93d8"; 
                break;
            case BATDEFENDER:  
                glowHex = "#64b5f6"; 
                break;
            case NIGHTSTALKER: 
                glowHex = "#ef5350"; 
                break;
            default: 
                return;
        }

        gc.setFill(Color.web(glowHex, 0.18));
        gc.fillOval(cx + 4, cy + 4, size - 8, size - 8);
    }

    //  Layer 3: emoji fallback 

    private static void drawEmoji(GraphicsContext gc, Creature creature,
                                   double cx, double cy, double size) {

        DropShadow shadow = new DropShadow(4, 1, 1, Color.WHITE);

        gc.setEffect(shadow);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Segoe UI Emoji", 32));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        gc.fillText(creature.type.emoji, cx + size / 2.0, cy + size / 2.0);

        gc.setEffect(null);
    }

    // ── HP bar helper (called from controller, not part of the three layers) ──

    /**
     * Draws a compact health bar at the bottom of the cell.
     * Green → yellow → red as health drops.
     */
    public static void drawHPBar(GraphicsContext gc, Creature c,
                                  double cellX, double cellY, double cellW) {
        double barY = cellY + cellW - 8;
        double barW = cellW - 8;
        double barX = cellX + 4;
        double fill = barW * ((double) c.health / c.maxHealth);

        gc.setFill(Color.web("#222222", 0.7));
        gc.fillRect(barX, barY, barW, 5);

        Color barColor;
        double ratio = (double) c.health / c.maxHealth;

        if (ratio > 0.6) {
            barColor = Color.web("#66bb6a");
        }      
        else if (ratio > 0.3) {
            barColor = Color.web("#ffa726");
        } 

        else {
            barColor = Color.web("#ef5350");
        }                   

        gc.setFill(barColor);
        gc.fillRect(barX, barY, fill, 5);
    }
}
