package de.ultical.backend.api;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import de.ultical.backend.api.transferClasses.RegisterRequest;
import de.ultical.backend.api.transferClasses.RegisterResponse;
import de.ultical.backend.api.transferClasses.RegisterResponse.RegisterResponseStatus;
import de.ultical.backend.app.RegistrationException;
import de.ultical.backend.app.RegistrationHandler;
import de.ultical.backend.model.Player;
import de.ultical.backend.model.User;

/**
 * Handle new user registration
 *
 * @author bas
 *
 */
@Path("/command/register")
public class RegisterResource {

    @Inject
    RegistrationHandler regHandler;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public RegisterResponse registerRequest(RegisterRequest registerRequest) throws Exception {

        // validate data
        if (registerRequest.getPassword().length() < 10) {
            return new RegisterResponse(RegisterResponseStatus.VALIDATION_ERROR);
        }

        try {
            final User user = this.regHandler.registerPlayer(registerRequest);
            final Player playerToRegister = user.getDfvPlayer();

            // return success code
            RegisterResponse response = new RegisterResponse(RegisterResponseStatus.SUCCESS);
            if (!user.isDfvEmailOptIn()) {
                // disguise email
                String[] emailParts = playerToRegister.getEmail().split("@");
                String disguisedLocalPart = "";
                for (int i = 0; i < emailParts[0].length(); i++) {
                    if (emailParts[0].length() <= 3 || i >= 3) {
                        disguisedLocalPart += "*";
                    } else {
                        disguisedLocalPart += emailParts[0].charAt(i);
                    }
                }
                response.setDfvEmail(disguisedLocalPart + "@" + emailParts[1]);
            }

            response.setUser(user);

            return response;
        } catch (RegistrationException re) {
            if (re.getPreparedResponse() != null) {
                return re.getPreparedResponse();
            } else if (re.getStatus() != null) {
                return new RegisterResponse(re.getStatus());
            } else {
                throw new WebApplicationException(re);
            }
        }
    }

}
