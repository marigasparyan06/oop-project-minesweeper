package wildhabitat.exceptions;

import java.io.IOException;

public class WaveLoadException extends IOException {
    public WaveLoadException(int lineNumber, String content) {
        super("Malformed data at line " + lineNumber + ": " + content);
    }

    public WaveLoadException(String message) {
        super(message);
    }
}
