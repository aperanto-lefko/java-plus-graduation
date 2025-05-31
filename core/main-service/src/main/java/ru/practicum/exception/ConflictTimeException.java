package ru.practicum.exception;

public class ConflictTimeException extends RuntimeException {
    public ConflictTimeException(String message) {
        super(message);
    }
}
