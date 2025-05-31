package ru.practicum.exception;


public class DataAlreadyInUseException extends RuntimeException {
    public DataAlreadyInUseException(String message) {
        super(message);
    }
}