package com.wildhabitat.exception;

/** Thrown when a save file contains a malformed CREATURE entry. */
public class SaveFileException extends RuntimeException {

    public SaveFileException(String message) {
        super(message);
    }
}
