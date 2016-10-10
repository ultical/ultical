package de.ultical.backend.exception;

public class IBANValidationException extends UlticalException {

    public IBANValidationException(String message) {
	super(message);
    }

    public IBANValidationException(String message, Throwable t) {
	super(message, t);
    }
}
