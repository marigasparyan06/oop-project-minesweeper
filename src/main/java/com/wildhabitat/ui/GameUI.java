package com.wildhabitat.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX application entry point.
 * Layout and all interaction logic live in GameUIController.
 *
 * Run with: mvn clean javafx:run
 */
public class GameUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        GameUIController controller = new GameUIController(primaryStage);
        Scene scene = new Scene(controller.buildRoot(), 920, 556);
        
        scene.getStylesheets(); // no external stylesheet — all style via setStyle()
        primaryStage.setTitle("WildHabitat");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        controller.initGame();
    }

    public static void main(String[] args) {
        launch(args);
    }
}