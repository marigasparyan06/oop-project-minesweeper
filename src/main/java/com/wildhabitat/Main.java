package com.wildhabitat;

import com.wildhabitat.cli.CLIGame;

public class Main {
    
    public static void main(String[] args) {
        String mode = (args.length > 0) ? args[0].toLowerCase() : "ui";

        switch (mode) {
            case "ui":
                launchUI(args);
                break;

            case "cli":
            default:
                CLIGame.start();
                break;
        }
    }

    private static void launchUI(String[] args) {
        try {
            com.wildhabitat.ui.GameUI.main(args);
        } 
        catch (NoClassDefFoundError | ExceptionInInitializerError e) {
            
            System.err.println("[ERROR] JavaFX runtime not found.");
            System.err.println("  Make sure JavaFX is on the module path:");
            System.err.println("  java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.graphics -jar wildhabitat.jar");
            System.err.println("  Falling back to CLI mode.");
            CLIGame.start();
        }
    }
}