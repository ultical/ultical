package de.ultical.backend.app;

import javax.inject.Inject;

import org.mindrot.jbcrypt.BCrypt;

import de.ultical.backend.api.transferClasses.RegisterRequest;
import de.ultical.backend.api.transferClasses.RegisterResponse.RegisterResponseStatus;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Player;
import de.ultical.backend.model.User;

/**
 * The default {@link RegistrationHandler} for ultical.
 * <p>
 * A {@link User} could be registered, if the following constraints are
 * satisfied:
 * <ul>
 * <li>The requested email has not been registered, yet</li>
 * <li>A "matching" {@link Player} is not registered to a {@link User}, yet</li>
 * </ul>
 * </p>
 * <p>
 * A <code>Player</code> is meant to match a <code>User</code> if the attributes
 * <code>firstName</code>,<code>lastName</code>, and <code>birthDate</code> are
 * equal.
 * </p>
 * 
 * @author bb
 *
 */
public class UltiCalRegistrationHandler implements RegistrationHandler {

    @Inject
    DataStore dataStore;

    @Override
    public User registerPlayer(RegisterRequest request) throws RegistrationException {
        if (isNullOrEmpty(request.getEmail()) || isNullOrEmpty(request.getFirstName())
                || isNullOrEmpty(request.getLastName())) {
            throw new RegistrationException("invalid input", RegisterResponseStatus.VALIDATION_ERROR);
        }
        try (AutoCloseable c = this.dataStore.getClosable()) {
            User user = this.dataStore.getUserByEmail(request.getEmail());
            if (user != null) {
                // email has already been registered.
                throw new RegistrationException("email already registered", RegisterResponseStatus.EMAIL_ALREADY_TAKEN);
            }
            user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(10)));
            return user;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private boolean isNullOrEmpty(final String input) {
        return input == null || input.isEmpty();
    }
}
