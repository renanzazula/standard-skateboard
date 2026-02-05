package com.skateboard.podcast.domain.exception;

public class UnauthorizedException extends DomainException {
    public UnauthorizedException(final String message) {
        super(message);
    }
}
