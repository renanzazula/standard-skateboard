package com.skateboard.podcast.standard.service.container.error;

import com.skateboard.podcast.domain.exception.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(final ValidationException exception) {
        final String message = exception == null ? "invalid request" : exception.getMessage();
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}
