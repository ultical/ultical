package de.ultical.backend.api;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.mindrot.jbcrypt.BCrypt;

import de.ultical.backend.api.transferClasses.AuthResponse;
import de.ultical.backend.api.transferClasses.AuthResponse.AuthResponseStatus;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.data.DataStore.DataStoreCloseable;
import de.ultical.backend.model.User;

/**
 * Handle new user registration
 *
 * @author bas
 *
 */
@Path("/command/auth")
public class AuthResource {

    @Inject
    DataStore dataStore;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AuthResponse AuthRequest(User requestedUser) {
        if (requestedUser == null) {
            return null;
        }
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }

        try (DataStoreCloseable c = this.dataStore.getClosable()) {

            User foundUser = this.dataStore.getUserByEmail(requestedUser.getEmail());

            if (foundUser == null) {
                return new AuthResponse(AuthResponseStatus.WRONG_CREDENTIALS);
            }

            if (!BCrypt.checkpw(requestedUser.getPassword(), foundUser.getPassword())) {
                return new AuthResponse(AuthResponseStatus.WRONG_CREDENTIALS);
            }

            if (!foundUser.isEmailConfirmed()) {
                return new AuthResponse(AuthResponseStatus.EMAIL_NOT_CONFIRMED);
            }

            if (!foundUser.isDfvEmailOptIn()) {
                return new AuthResponse(AuthResponseStatus.DFV_EMAIL_NOT_OPT_IN);
            }

            AuthResponse successResponse = new AuthResponse(AuthResponseStatus.SUCCESS);
            successResponse.setUser(foundUser);

            return successResponse;
        }
    }

}
