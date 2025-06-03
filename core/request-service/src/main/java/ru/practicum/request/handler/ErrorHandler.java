package ru.practicum.request.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.handler.BaseErrorHandler;
@Slf4j
@RestControllerAdvice
public class ErrorHandler extends BaseErrorHandler {
}
