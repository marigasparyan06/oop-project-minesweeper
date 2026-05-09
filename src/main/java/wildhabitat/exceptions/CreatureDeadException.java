package wildhabitat.exceptions;

public class CreatureDeadException extends RuntimeException {
    public CreatureDeadException(String creatureName) {
        super("Action called on dead creature: " + creatureName);
    }
}
