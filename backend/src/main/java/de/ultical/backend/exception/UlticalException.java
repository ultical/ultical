package de.ultical.backend.exception;

/**
 * Base class for ultical-related exceptions.
 * 
 * @author Basil
 */
public class UlticalException extends RuntimeException {
    public UlticalException(final String message) {
	super(message);
    }

    public UlticalException(final String message, final Throwable t) {
	super(message, t);
    }
}
