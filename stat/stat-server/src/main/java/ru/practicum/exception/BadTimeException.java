package ru.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadTimeException extends RuntimeException {
    public BadTimeException(String message) {
        super(message);
    }
}
