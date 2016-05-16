package de.ultical.backend.app;

import de.ultical.backend.api.transferClasses.RegisterResponse;
import de.ultical.backend.api.transferClasses.RegisterResponse.RegisterResponseStatus;

/**
 * This regsitration will be thrown, whenever a problem within the registration
 * of a player occurs. Mainly within the instances of the
 * {@link RegistrationHandler}.
 *
 * @author bb
 *
 */
public class RegistrationException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 693984696663497397L;

    private final RegisterResponseStatus status;
    private final RegisterResponse preparedResponse;

    public RegistrationException(String message, RegisterResponseStatus stat, RegisterResponse resp) {
        super(message);
        this.status = stat;
        this.preparedResponse = resp;
    }

    public RegistrationException(String message, RegisterResponseStatus stat) {
        this(message, stat, null);
    }

    public RegistrationException(String message) {
        this(message, null, null);
    }

    public RegisterResponseStatus getStatus() {
        return this.status;
    }

    public RegisterResponse getPreparedResponse() {
        return this.preparedResponse;
    }
}
