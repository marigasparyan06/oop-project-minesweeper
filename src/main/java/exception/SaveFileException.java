package exception;

import java.io.IOException;

/** Thrown when a save file contains malformed or unrecognisable data. */
public class SaveFileException extends IOException {

    public SaveFileException(String message) {
        super(message);
    }

    public SaveFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
