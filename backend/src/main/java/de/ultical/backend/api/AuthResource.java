/**
 * Copyright (C) 2015-2016 ultical contributors
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 * * Apache License, version 2.0
 * * Apache Software License, version 1.0
 * * Mozilla Public License, versions 1.0, 1.1 and 2.0
 * * Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU Affero General Public License
 * version 3 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the  GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
    public AuthResponse AuthRequest(User requestedUser) throws Exception {
        if (requestedUser == null) {
            return null;
        }
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }

        try (AutoCloseable c = this.dataStore.getClosable()) {

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
