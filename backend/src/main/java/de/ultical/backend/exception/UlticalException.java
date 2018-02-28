package de.ultical.backend.exception;

/**
 * Base class for ultical-related exceptions.
 * 
 * @author Basil
 */
public class UlticalException extends Exception {

	private static final long serialVersionUID = -4584945590554496711L;

	public UlticalException(final String message) {
		super(message);
	}

	public UlticalException(final String message, final Throwable t) {
		super(message, t);
	}
}
