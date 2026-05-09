package wildhabitat.exceptions;

public class InvalidPlacementException extends Exception {
    public InvalidPlacementException(String creatureType, String tileType) {
        super("Cannot place " + creatureType + " on " + tileType + " tile");
    }

    public InvalidPlacementException(String message) {
        super(message);
    }
}
