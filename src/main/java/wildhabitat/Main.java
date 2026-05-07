package wildhabitat;

import wildhabitat.cli.CLIGame;
import wildhabitat.ui.GameUI;

public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("cli")) {
            CLIGame.start();
        } 
        else {
            GameUI.launch(args);
        }
    }
}
