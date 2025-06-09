package ru.practicum.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.ConflictStateException;
import ru.practicum.exception.ConflictTimeException;
import ru.practicum.exception.DataAlreadyInUseException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ServiceUnavailableException;
import ru.practicum.exception.ValidationException;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestControllerAdvice
public class BaseErrorHandler {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {
        return handleException(e, HttpStatus.NOT_FOUND, "Объект не найден");
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return handleException(e, HttpStatus.CONFLICT, "Нарушение целостности данных");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return handleException(e, HttpStatus.BAD_REQUEST, "Запрос составлен некорректно");
    }

    @ExceptionHandler(DataAlreadyInUseException.class)
    public ResponseEntity<ErrorResponse> handleDataAlreadyInUseException(DataAlreadyInUseException e) {
        return handleException(e, HttpStatus.CONFLICT, "Данные уже используются");
    }

    @ExceptionHandler(ConflictStateException.class)
    public ResponseEntity<ErrorResponse> handleConflictEventStateException(ConflictStateException e) {
        return handleException(e, HttpStatus.CONFLICT, "Конфликт статуса события");
    }

    @ExceptionHandler(ConflictTimeException.class)
    public ResponseEntity<ErrorResponse> handleConflictEventTimeException(ConflictTimeException e) {
        return handleException(e, HttpStatus.CONFLICT, "Конфликт времени события");
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e) {
        return handleException(e, HttpStatus.BAD_REQUEST, "Ошибка валидации данных");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return handleException(e, HttpStatus.CONFLICT, "Некорректное значение аргумента");
    }

    @ExceptionHandler(ConditionsNotMetException.class)
    public ResponseEntity<ErrorResponse> handleConditionsNotMetException(ConditionsNotMetException e) {
        return handleException(e, HttpStatus.CONFLICT, "Условия не выполнены");
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailableException(ServiceUnavailableException e) {
        return handleException(e, HttpStatus.BAD_REQUEST, "Сервис не доступен");
    }

    // Общий метод обработки исключений
    private <T extends Exception> ResponseEntity<ErrorResponse> handleException(T e, HttpStatus status, String reason) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        logging(e, status, timestamp);
        return new ResponseEntity<>(new ErrorResponse(status.name(), reason, e.getMessage(), timestamp), status);
    }


    private void logging(Exception e, HttpStatus status, String timestamp) {
        log.error("{} - Status: {}, Description: {}, Timestamp: {}",
                e.getClass().getSimpleName(), status, e.getMessage(), timestamp);
    }
}