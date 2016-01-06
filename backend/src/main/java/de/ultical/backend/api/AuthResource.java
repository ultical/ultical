package de.ultical.backend.api;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.mindrot.jbcrypt.BCrypt;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.User;

/**
 * Handle new user registration
 *
 * @author bas
 *
 */
@Path("/auth")
public class AuthResource {

    @Inject
    DataStore dataStore;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User AuthRequest(User requestedUser) {
        if (requestedUser == null) {
            return null;
        }

        User foundUser = this.dataStore.getUserByEmail(requestedUser.getEmail());

        if (foundUser == null) {
            return null;
        }

        if (!BCrypt.checkpw(requestedUser.getPassword(), foundUser.getPassword())) {
            return null;
        }

        return foundUser;
    }

}
