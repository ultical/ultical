package de.ultical.backend.app;

import de.ultical.backend.api.transferClasses.RegisterRequest;
import de.ultical.backend.model.User;

public interface RegistrationHandler {

    User registerPlayer(final RegisterRequest request) throws RegistrationException;
}
