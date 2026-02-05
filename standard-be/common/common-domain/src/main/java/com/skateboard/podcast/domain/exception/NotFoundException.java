package com.skateboard.podcast.domain.exception;

public class NotFoundException extends DomainException {
    public NotFoundException(final String message) {
        super(message);
    }
}
