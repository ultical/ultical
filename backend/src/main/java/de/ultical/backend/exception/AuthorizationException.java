package de.ultical.backend.exception;

public class AuthorizationException extends UlticalException {

    public AuthorizationException(final String message) {
	super(message);
    }

    public AuthorizationException(final String message, final Throwable t) {
	super(message, t);
    }
}
