package com.skateboard.podcast.domain.exception;

public class ValidationException extends DomainException {
    public ValidationException(final String message) {
        super(message);
    }
}
