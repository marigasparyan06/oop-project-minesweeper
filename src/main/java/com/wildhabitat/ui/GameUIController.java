package com.wildhabitat.ui;

import com.wildhabitat.engine.GameEngine;
import com.wildhabitat.exception.InvalidPlacementException;
import com.wildhabitat.io.GridLoader;
import com.wildhabitat.io.SaveManager;
import com.wildhabitat.model.*;
import com.wildhabitat.renderer.CreatureRenderer;

import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

/**
 * Builds and drives the full JavaFX UI.
 * All layout is in Java code — no FXML.
 *
 * Grid layout: 7 rows × 11 cols, each cell 70×70 px.
 * Attackers spawn at col 10 and move left toward col 0.
 */
public class GameUIController {

    private static final int CELL = 70;
    private static final int ROWS = GameState.ROWS;
    private static final int COLS = GameState.COLS;

    private GameState  state;
    private GameEngine engine;

    // Top bar 
    private HBox  topBar;
    private Label phaseLabel;
    private Label statsLabel;
    private String currentTopBarHex = TimeOfDay.DAY.topBarHex();

    // Left panel 
    private VBox leftPanel;
    private CreatureType selectedDefender       = null;
    private Button       selectedDefenderButton = null;

    // Grid 
    private StackPane   gridContainer;
    private GridPane    gridPane;
    private StackPane[][] cellPanes;
    private Canvas[][]   cellCanvases;

    // Cell selection
    private int       selectedRow   = -1;
    private int       selectedCol   = -1;
    private Rectangle selectionRect = null;
    private Timeline  selectionPulse = null;

    //  Right panel 
    private Button playPauseBtn;
    private Timeline gameLoop;
    private boolean  isPlaying = false;

    // Bottom log 
    private ObservableList<String> logItems;
    private ListView<String> logView;


    public GameUIController(Stage stage) {
        // stage retained by GameUI, not needed here
    }

    //  Layout builders 

    /** Assembles the full BorderPane used as the scene root. */
    public BorderPane buildRoot() {
        BorderPane root = new BorderPane();
        
        root.setStyle("-fx-background-color: #0f1f0f;");
        root.setTop(buildTopBar());
        root.setLeft(buildLeftPanel());
        root.setCenter(buildGridArea());
        root.setRight(buildRightPanel());
        root.setBottom(buildBottomLog());
       
        return root;
    }

    //  Top bar 

    private HBox buildTopBar() {

        topBar = new HBox(14);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 16, 0, 16));
        topBar.setPrefHeight(50);
        topBar.setStyle("-fx-background-color: " + currentTopBarHex + ";");
        topBar.setEffect(new DropShadow(10, 0, 4, Color.BLACK));

        phaseLabel = new Label("☀ DAY");
        phaseLabel.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 16));
        phaseLabel.setTextFill(Color.WHITE);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.3);");

        statsLabel = new Label();
        statsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        statsLabel.setTextFill(Color.WHITE);

        topBar.getChildren().addAll(phaseLabel, sep, statsLabel);
        return topBar;
    }

    //  Left panel 

    private VBox buildLeftPanel() {
        leftPanel = new VBox(6);
        leftPanel.setPrefWidth(160);
        leftPanel.setPadding(new Insets(10, 8, 10, 8));
        leftPanel.setStyle("-fx-background-color: #1e3a1e;");
        leftPanel.setEffect(new DropShadow(10, 0, 4, Color.BLACK));

        Label title = new Label("DEFENDERS");

        title.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        title.setTextFill(Color.web("#aed581"));
        title.setPadding(new Insets(0, 0, 4, 0));
        leftPanel.getChildren().add(title);

        for (CreatureType type : CreatureType.defenders()) {
            leftPanel.getChildren().add(buildDefenderButton(type));
        }

        return leftPanel;
    }

    private Button buildDefenderButton(CreatureType type) {
        Canvas mini = new Canvas(28, 28);
        GraphicsContext gc = mini.getGraphicsContext2D();

        gc.setFill(Color.web("#1e3a1e"));
        gc.fillRect(0, 0, 28, 28);
        
        CreatureRenderer.render(gc, Creature.create(type, 0, 0), TimeOfDay.DAY, 0, 0, 28);

        Label nameLbl = new Label(type.displayName);
        nameLbl.setFont(Font.font("Arial", 11));
        nameLbl.setTextFill(Color.WHITE);

        Label costLbl = new Label("⚡ " + type.energyCost);
        costLbl.setFont(Font.font("Arial", 10));
        costLbl.setTextFill(Color.web("#ffd54f"));

        VBox text = new VBox(1, nameLbl, costLbl);
        HBox inner = new HBox(6, mini, text);
        inner.setAlignment(Pos.CENTER_LEFT);

        Button btn = new Button();
        
        btn.setGraphic(inner);
        btn.setPrefWidth(144);
        btn.setPrefHeight(44);
        btn.setStyle(btnStyleNormal());

        btn.setOnMouseEntered(e -> { if (selectedDefender != type) btn.setStyle(btnStyleHover()); });
        btn.setOnMouseExited( e -> { if (selectedDefender != type) btn.setStyle(btnStyleNormal()); });
        btn.setOnAction(e -> handleDefenderSelect(type, btn));
        return btn;
    }

    private String btnStyleNormal() {
        return "-fx-background-color: #1e3a1e; -fx-text-fill: white;"
             + "-fx-background-radius: 8; -fx-border-radius: 8;"
             + "-fx-border-color: #2d5a27; -fx-border-width: 1;"
             + "-fx-cursor: hand; -fx-padding: 4 6 4 6;";
    }
    private String btnStyleHover() {
        return "-fx-background-color: #2d5a27; -fx-text-fill: white;"
             + "-fx-background-radius: 8; -fx-border-radius: 8;"
             + "-fx-border-color: #2d5a27; -fx-border-width: 1;"
             + "-fx-cursor: hand; -fx-padding: 4 6 4 6;";
    }
    private String btnStyleSelected() {
        return "-fx-background-color: #1e3a1e; -fx-text-fill: white;"
             + "-fx-background-radius: 8; -fx-border-radius: 8;"
             + "-fx-border-color: #f9a825; -fx-border-width: 0 0 0 4;"
             + "-fx-cursor: hand; -fx-padding: 4 6 4 6;";
    }

    private void handleDefenderSelect(CreatureType type, Button btn) {
        if (selectedDefenderButton != null) {
            selectedDefenderButton.setStyle(btnStyleNormal());
        } 
       
        if (selectedDefender == type) {
            selectedDefender = null;
            selectedDefenderButton = null;
        } 
        else {
            selectedDefender = type;
            selectedDefenderButton = btn;
            btn.setStyle(btnStyleSelected());
        }
    }

    //Grid area 

    private StackPane buildGridArea() {
        gridPane = new GridPane();
        cellPanes = new StackPane[ROWS][COLS];
        cellCanvases = new Canvas[ROWS][COLS];

        for (int r = 0; r < ROWS; ++r) {
            for (int c = 0; c < COLS; ++c) {
                Canvas canvas = new Canvas(CELL, CELL);
                cellCanvases[r][c] = canvas;

                // Transparent click catcher above the canvas
                Pane clickTarget = new Pane();
           
                clickTarget.setPrefSize(CELL, CELL);
                clickTarget.setStyle("-fx-background-color: transparent;");

                StackPane cell = new StackPane(canvas, clickTarget);
            
                cell.setPrefSize(CELL, CELL);
                cellPanes[r][c] = cell;

                final int fr = r, fc = c;

                clickTarget.setOnMouseClicked(e -> handleCellClick(fr, fc));

                gridPane.add(cell, c, r);
            }
        }

        gridContainer = new StackPane(gridPane);
        gridContainer.setAlignment(Pos.TOP_LEFT);
        gridContainer.setEffect(new DropShadow(10, 0, 4, Color.BLACK));
       
        return gridContainer;
    }

    //  Right panel 
    

    private VBox buildRightPanel() {
        VBox panel = new VBox(10);
     
        panel.setPrefWidth(140);
        panel.setPadding(new Insets(12, 10, 12, 10));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setStyle("-fx-background-color: #3a6b35;");
        panel.setEffect(new DropShadow(10, 0, 4, Color.BLACK));

        playPauseBtn = makeCtrlBtn("▶ Start");
     
        Button saveBtn     = makeCtrlBtn("💾 Save");
        Button loadBtn     = makeCtrlBtn("📂 Load");
        Button waveBtn     = makeCtrlBtn("⚡ Load Wave");

        playPauseBtn.setOnAction(e -> togglePlay());
        saveBtn.setOnAction(e -> doSave());
        loadBtn.setOnAction(e -> doLoad());
        waveBtn.setOnAction(e -> doLoadWave());

        panel.getChildren().addAll(playPauseBtn, saveBtn, loadBtn, waveBtn);

     
        if ("true".equals(System.getProperty("debug"))) {
            Button skipPhase = makeCtrlBtn("[D] Skip Phase");
     
            skipPhase.setStyle(skipPhase.getStyle()
                    + "-fx-background-color: #7b1fa2;");
            skipPhase.setOnAction(e -> doSkipPhase());
            panel.getChildren().add(skipPhase);
        }

        return panel;
    }

    private Button makeCtrlBtn(String text) {
        Button btn = new Button(text);
   
        btn.setPrefWidth(120);
        btn.setPrefHeight(36);
        btn.setFont(Font.font("Segoe UI Emoji", 12));
   
        String base = "-fx-background-color: #2d5a27; -fx-text-fill: white;"
                    + "-fx-background-radius: 6; -fx-border-radius: 6;"
                    + "-fx-cursor: hand; -fx-padding: 4 8 4 8;";
        String hover = "-fx-background-color: #4a7c59; -fx-text-fill: white;"
                     + "-fx-background-radius: 6; -fx-border-radius: 6;"
                     + "-fx-cursor: hand; -fx-padding: 4 8 4 8;";
       
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited( e -> btn.setStyle(base));
        
        return btn;
    }

    // Bottom log 

    private VBox buildBottomLog() {
        logItems = FXCollections.observableArrayList();
     
        logView = new ListView<>(logItems);
        logView.setPrefHeight(90);
        logView.setStyle("-fx-background-color: #111f11; -fx-control-inner-background: #111f11;");

        logView.setCellFactory(lv -> new ListCell<>() {
     
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
               
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #111f11;");
                    return;
                }
             
                setText(item);
                setFont(Font.font("Monospaced", 12));
                setStyle("-fx-background-color: #111f11; -fx-padding: 1 4 1 4;");
               
                if (item.startsWith("[MOVE]"))   {
                    setTextFill(Color.web("#00bcd4"));
                } 
                else if (item.startsWith("[ATTACK]")) {
                    setTextFill(Color.web("#ef5350"));
                    
                } 
                else if (item.startsWith("[DEFEND]")) {
                    setTextFill(Color.web("#66bb6a"));
                } 
               
                else if (item.startsWith("[PHASE]"))  {
                    setTextFill(Color.web("#ffa726"));
                } 
               
                else if (item.startsWith("[WAVE]"))   {
                    setTextFill(Color.web("#ffa726"));                    
                } 
                
                else if (item.startsWith("[PLACE]"))  {
                    setTextFill(Color.web("#b2ebf2"));                    
                } 
             
                else if (item.startsWith("[INFO]"))   {
                    setTextFill(Color.web("#90caf9"));                    
                } 

                else {
                    setTextFill(Color.web("#cccccc"));
                }                                   
            }
        });

        VBox box = new VBox(logView);

        box.setStyle("-fx-background-color: #111f11;");
        
        return box;
    }

    //  Initialisation 

    /** Called after the Stage is shown. Safe to call snapshot/layout methods here. */
    public void initGame() {
        state  = new GameState();
        engine = new GameEngine(state);

        try {
            GridLoader.loadDefault(state);
        } 
        catch (IOException e) {
            fillBlankTerrain();
            addLog("[GAME] gridstate.txt not found — using blank terrain.");
        }

        try {
            SaveManager.loadDefault(state);
            addLog("[GAME] Save loaded.");
        } 
        catch (IOException e) {
            addLog("[GAME] No save file — starting Wave 1.");
            engine.triggerNextWave();
            
            for (String m : state.messageLog) {
                addLog(m);
            } 
        }

        gameLoop = new Timeline(new KeyFrame(Duration.millis(900), e -> advanceTurn()));
        gameLoop.setCycleCount(Timeline.INDEFINITE);

        redrawAll();
        updateTopBar();
    }

    // Turn processing 

    private void advanceTurn() {
        if (engine.isGameOver()) {
            return;
        } 

        List<String> msgs = engine.nextTurn();

        // Log messages first so the player can read what happened before the redraw
        for (String m : msgs) {
            addLog(m);
        } 

        // Animate attacked cells (brief red flash)
        for (int[] cell : engine.getLastAttackedCells()) {
            flashCell(cell[0], cell[1], Color.web("#ef5350", 0.55), 200);
        }

        // Rabbit hop animation on cells that just moved
        for (int[] pos : engine.getLastMovedCells()) {
            
            Creature c = state.getCreatureAt(pos[0], pos[1]);
            
            if (c != null && c.type == CreatureType.RABBIT) {
                doRabbitHop(pos[0], pos[1]);
            }
        }

        redrawAll();
        updateTopBar();

        if (engine.wasPhaseChangedThisTurn()) {
            animateTopBarColor(state.timeOfDay.topBarHex());
            animatePhaseLabel(state.timeOfDay);
            doPhaseTransitionOverlay(state.timeOfDay);
        }

        // Game-over check, stop the loop and show the overlay
        if (engine.isGameOver()) {

            if (isPlaying) {
                gameLoop.stop();
                isPlaying = false;
                playPauseBtn.setText("▶ Start");
            }
            
            showGameOverOverlay(engine.didPlayerWin());
        }
    }

    //Cell click handler 

    private void handleCellClick(int row, int col) {
        if (selectedDefender != null) {
            try {
                // placeDefender throws InvalidPlacementException on illegal moves
                String result = engine.placeDefender(selectedDefender, row, col);
                addLog(result);
                redrawCell(row, col);
                doPlaceDefenderAnim(row, col);
                updateTopBar();
            }
            catch (InvalidPlacementException e) {
                addLog("[ERROR] " + e.getMessage());
            }
            return;
        }

        // No defender selected, just select the cell to inspect it
        clearSelection();
       
        selectedRow = row;
        selectedCol = col;

        selectionRect = new Rectangle(66, 66);
 
        selectionRect.setFill(Color.TRANSPARENT);
        selectionRect.setStroke(Color.web("#f9a825"));
        selectionRect.setStrokeWidth(3);
        selectionRect.setMouseTransparent(true);
 
        cellPanes[row][col].getChildren().add(selectionRect);

        // Pulse via opacity animation 
        selectionPulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(selectionRect.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(600), new KeyValue(selectionRect.opacityProperty(), 0.2)),
                new KeyFrame(Duration.millis(1200), new KeyValue(selectionRect.opacityProperty(), 1.0))
        );

        selectionPulse.setCycleCount(Timeline.INDEFINITE);
        selectionPulse.play();

        Creature c = state.getCreatureAt(row, col);

        if (c != null) {
            addLog("[INFO] " + c.type.displayName + "  HP " + c.health + "/" + c.maxHealth
                    + "  @ (" + row + "," + col + ")");
        } 
        else {
            addLog("[INFO] (" + row + "," + col + ") — " + state.terrain[row][col].name());
        }
    }

    private void clearSelection() {
        if (selectionPulse != null) { 
            selectionPulse.stop(); selectionPulse = null; 
        }

        if (selectionRect != null && selectedRow >= 0 && selectedCol >= 0) {
            cellPanes[selectedRow][selectedCol].getChildren().remove(selectionRect);
            selectionRect = null;
        }

        selectedRow = -1;
        selectedCol = -1;
    }

    //Rendering 

    private void redrawAll() {

        for (int r = 0; r < ROWS; ++r) {
            for (int c = 0; c < COLS; ++c) {
                redrawCell(r, c);
            }
        }
    }

    private void redrawCell(int row, int col) {
        Canvas canvas = cellCanvases[row][col];
        GraphicsContext gc = canvas.getGraphicsContext2D();
  
        gc.clearRect(0, 0, CELL, CELL);

        // Terrain base colour 
        Terrain terrain = state.terrain[row][col];
        
        gc.setFill(Color.web(terrain.colorHex()));
        gc.fillRect(0, 0, CELL, CELL);

        // Phase tint 
        switch (state.timeOfDay) {
            case NIGHT:
                gc.setFill(Color.rgb(0, 0, 0, 0.55));
                gc.fillRect(0, 0, CELL, CELL);
                break;
            case DAWN:
                gc.setFill(Color.web("#ff8c3c", 0.15));
                gc.fillRect(0, 0, CELL, CELL);
                break;
            case DUSK:
                gc.setFill(Color.web("#c86414", 0.25));
                gc.fillRect(0, 0, CELL, CELL);
                break;
            default:
                break; // DAY, fully saturated, no overlay
        }

        // Creature (three-layer renderer) 
        Creature creature = state.getCreatureAt(row, col);
   
        if (creature != null) {
            CreatureRenderer.render(gc, creature, state.timeOfDay, 0, 0, CELL);
   
            if (creature.health < creature.maxHealth) {
                CreatureRenderer.drawHPBar(gc, creature, 0, 0, CELL);
            }
        }

        // Subtle grid line 
        gc.setStroke(Color.web("#000000", 0.22));
        gc.setLineWidth(1);
        gc.strokeRect(0.5, 0.5, CELL - 1, CELL - 1);
    }

    // UI state helpers 

    private void updateTopBar() {
        phaseLabel.setText(phaseIcon(state.timeOfDay) + " " + state.timeOfDay.label());
        statsLabel.setText("Energy: " + state.energy
                + "  |  Score: " + state.score
                + "  |  Turn: "  + state.turn
                + "  |  Wave: "  + state.wave);
    }

    private String phaseIcon(TimeOfDay phase) {
        switch (phase) {
            case DAWN:  
                return "🌅";
            case DAY:   
                return "☀";
            case DUSK:  
                return "🌆";
            case NIGHT: 
                return "🌙";
            default:    
                return "";
        }
    }

    private void addLog(String msg) {
        logItems.add(msg);

        while (logItems.size() > 50) {
            logItems.remove(0);
        } 

        logView.scrollTo(logItems.size() - 1);
    }

    //  Animations 

    /** Brief coloured flash on a cell — used for attack hits. */
    private void flashCell(int row, int col, Color color, int ms) {
        Rectangle flash = new Rectangle(CELL, CELL, color);
        
        flash.setMouseTransparent(true);
        cellPanes[row][col].getChildren().add(flash);
        
        FadeTransition ft = new FadeTransition(Duration.millis(ms), flash);
        
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> cellPanes[row][col].getChildren().remove(flash));
        ft.play();
    }

    /** Scale-up when a defender is placed (0.8 → 1.0 over 150 ms). */
    private void doPlaceDefenderAnim(int row, int col) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), cellPanes[row][col]);
      
        st.setFromX(0.8); st.setFromY(0.8);
        st.setToX(1.0);   st.setToY(1.0);
        st.play();
    }

    /** Rabbit bounce — tiny Y hop each time it moves. */
    private void doRabbitHop(int row, int col) {
        TranslateTransition hop = new TranslateTransition(Duration.millis(60), cellPanes[row][col]);
        
        hop.setByY(-4);
        hop.setAutoReverse(true);
        hop.setCycleCount(2);
        hop.setOnFinished(e -> cellPanes[row][col].setTranslateY(0));
        hop.play();
    }

    /**
     * Full-grid phase-colour overlay: fades in at 30% opacity, then fades to transparent
     * over one second — creates a visual "flash" on phase change.
     */
    private void doPhaseTransitionOverlay(TimeOfDay phase) {
        double w = (double) COLS * CELL;
        double h = (double) ROWS * CELL;
   
        Rectangle overlay = new Rectangle(w, h, Color.web(phase.topBarHex(), 0.30));
   
        overlay.setMouseTransparent(true);
        gridContainer.getChildren().add(overlay);
   
        FadeTransition ft = new FadeTransition(Duration.seconds(1), overlay);
   
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> gridContainer.getChildren().remove(overlay));
        ft.play();
    }

    /**
     * Smoothly interpolates the top bar background colour over 800 ms.
     * Uses a Timeline with 32 KeyFrames to compute intermediate colours via Color.interpolate().
     */
    private void animateTopBarColor(String targetHex) {
        Color from = Color.web(currentTopBarHex);
        Color to   = Color.web(targetHex);
        int steps  = 32;
        KeyFrame[] frames = new KeyFrame[steps + 1];
        
        for (int i = 0; i <= steps; i++) {
            double t   = (double) i / steps;
            Color mid  = from.interpolate(to, t);
            String hex = colorToHex(mid);
        
        
            final String h = hex;
        
            frames[i] = new KeyFrame(Duration.millis(800.0 / steps * i),
                    e -> topBar.setStyle("-fx-background-color: " + h + ";"));
        }
        
        new Timeline(frames).play();
        
        currentTopBarHex = targetHex;
    }

    /** Cross-fade the phase label text: fade out, swap text, fade in. */
    private void animatePhaseLabel(TimeOfDay newPhase) {
        FadeTransition out = new FadeTransition(Duration.millis(200), phaseLabel);
      
        out.setFromValue(1.0);
        out.setToValue(0.0);
        out.setOnFinished(e -> {
      
            phaseLabel.setText(phaseIcon(newPhase) + " " + newPhase.label());
            FadeTransition in = new FadeTransition(Duration.millis(400), phaseLabel);
      
            in.setFromValue(0.0);
            in.setToValue(1.0);
            in.play();
      
        });
      
        out.play();
    }

    /**
     * Game-over overlay slides down from the top of the grid (TranslateTransition, 400 ms).
     * Contains win/lose title, final score, and a "Play Again" button that fully resets state.
     */
    private void showGameOverOverlay(boolean win) {
        double w = (double) COLS * CELL;
        double h = (double) ROWS * CELL;

        StackPane overlay = new StackPane();
    
        overlay.setPrefSize(w, h);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.82);");

        Label title = new Label(win ? "YOU WIN!" : "GAME OVER");
      
        title.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        title.setTextFill(win ? Color.web("#66bb6a") : Color.web("#ef5350"));

        Label scoreLbl = new Label("Final Score: " + state.score);
     
        scoreLbl.setFont(Font.font("Arial", 22));
        scoreLbl.setTextFill(Color.WHITE);

        Button again = new Button("▶  Play Again");
     
        again.setFont(Font.font("Arial", 16));
        again.setStyle("-fx-background-color: #2d5a27; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 8 24 8 24; -fx-cursor: hand;");
        again.setOnAction(e -> {
            gridContainer.getChildren().remove(overlay);
            logItems.clear();
            initGame();
        });

        VBox content = new VBox(14, title, scoreLbl, again);
        content.setAlignment(Pos.CENTER);
        overlay.getChildren().add(content);

        // Start off-screen above the grid, then slide down
        overlay.setTranslateY(-h);
        gridContainer.getChildren().add(overlay);

        TranslateTransition slide = new TranslateTransition(Duration.millis(400), overlay);
        slide.setToY(0);
        slide.play();
    }

    // Control button handlers 

    private void togglePlay() {
        if (isPlaying) {
            gameLoop.stop();
            isPlaying = false;
            playPauseBtn.setText("▶ Start");
        } 
        else {
            gameLoop.play();
            isPlaying = true;
            playPauseBtn.setText("⏸ Pause");
        }
    }

    private void doSave() {
        // snapshotCreatures() uses Creature.clone() to capture a point-in-time copy
        List<Creature> snapshot = state.snapshotCreatures();
        try {
            SaveManager.saveDefault(state);
            addLog("[GAME] Saved " + snapshot.size() + " creatures to savegame.txt.");
        }
        catch (IOException e) {
            addLog("[ERROR] Save failed: " + e.getMessage());
        }
    }

    private void doLoad() {
        try {
            GridLoader.loadDefault(state);
            SaveManager.loadDefault(state);
            addLog("[GAME] Loaded from savegame.txt.");
            redrawAll();
            updateTopBar();
        } 
        catch (IOException e) {
            addLog("[ERROR] Load failed: " + e.getMessage());
        }
    }

    private void doLoadWave() {
        if (isPlaying) { 
            gameLoop.stop(); isPlaying = false; playPauseBtn.setText("▶ Start"); 
        }
        
        state.messageLog.clear();
        engine.triggerNextWave();
        
        for (String m : state.messageLog) {
            addLog(m);
        } 

        redrawAll();
        updateTopBar();
    }

    /** Debug: force-advance the phase by exhausting the phase tick counter. */
    private void doSkipPhase() {
        state.dayCycleTick = GameState.PHASE_DURATION - 1;
        
        advanceTurn(); // next turn will then tick over
    }

    //  Utilities 

    private static String colorToHex(Color c) {
        return String.format("#%02x%02x%02x",
                (int)(c.getRed() * 255),
                (int)(c.getGreen() * 255),
                (int)(c.getBlue() * 255));
    }

    private void fillBlankTerrain() {
        for (int r = 0; r < ROWS; ++r) {
            for (int c = 0; c < COLS; ++c) {
                state.terrain[r][c] = Terrain.GRASS;
            }
        }
    }
}
