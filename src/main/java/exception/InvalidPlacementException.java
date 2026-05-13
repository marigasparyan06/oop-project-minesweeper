package exception;

/** Thrown when a defender cannot be placed (occupied cell, out of bounds, insufficient energy). */
public class InvalidPlacementException extends RuntimeException {

    public InvalidPlacementException(String message) {
        super(message);
    }
}
