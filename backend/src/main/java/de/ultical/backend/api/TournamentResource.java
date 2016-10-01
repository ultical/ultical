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

import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.ibatis.exceptions.PersistenceException;

import de.ultical.backend.app.Authenticator;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.DivisionRegistration.DivisionRegistrationStatus;
import de.ultical.backend.model.TeamRegistration;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.User;
import io.dropwizard.auth.Auth;

@Path("/tournaments")
public class TournamentResource {

    @Inject
    DataStore dataStore;

    private void checkDataStore() {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency injection for datastore failed!",
                    Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TournamentEdition> getAllTournaments() {
        this.checkDataStore();
        try {
            List<TournamentEdition> result = this.dataStore.getAll(TournamentEdition.class);
            return result;
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed", pe, Status.INTERNAL_SERVER_ERROR);
        }

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public TournamentEdition storeTournament(final TournamentEdition newEdition) {
        this.checkDataStore();
        try {
            TournamentEdition result = this.dataStore.addNew(newEdition);
            return result;
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed", pe, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/{editionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateTournament(final @PathParam("editionId") Integer editionId, TournamentEdition edition) {
        this.checkDataStore();
        try {
            if (editionId.equals(edition.getId()) == false) {
                throw new WebApplicationException("Request URL and payload do not match!", Status.NOT_ACCEPTABLE);
            }
            boolean updated = this.dataStore.update(edition);
            if (!updated) {
                throw new WebApplicationException(
                        "Update failed, eventually someone else update the resource before you", Status.CONFLICT);
            }

        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed", pe, Status.INTERNAL_SERVER_ERROR);
        }
    }

    /*
     * REGISTRATION
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/division/{divisionRegistrationId}/register/team")
    public TeamRegistration registerTeam(@PathParam("divisionRegistrationId") Integer divisionRegistrationId,
            TeamRegistration teamRegistration, @Auth @NotNull User currentUser) throws Exception {

        this.checkDataStore();

        try (AutoCloseable c = this.dataStore.getClosable()) {
            Authenticator.assureRosterAdmin(this.dataStore, teamRegistration.getRoster().getId(), currentUser);

            teamRegistration.setPaid(false);
            teamRegistration.setStatus(DivisionRegistrationStatus.PENDING);
            teamRegistration.setNotQualified(false);
            teamRegistration.setSpiritScore(-1);
            teamRegistration.setSequence(-1);
            teamRegistration.setStanding(-1);

            return this.dataStore.registerTeamForEdition(divisionRegistrationId, teamRegistration);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Probably duplicate entry" + pe.getMessage(), Status.CONFLICT);
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/registration/{eventId}")
    public boolean updateTeamRegistration(@PathParam("eventId") Integer eventId, TeamRegistration teamRegistration,
            @Auth @NotNull User currentUser) throws Exception {

        this.checkDataStore();

        try (AutoCloseable c = this.dataStore.getClosable()) {

            TournamentEdition edition = this.dataStore.getEditionByTeamRegistration(teamRegistration.getId());

            if (eventId > 0) {
                if (!edition.isAllowEventTeamRegManagement()) {
                    throw new WebApplicationException("You are not allowed to make those changes", Status.FORBIDDEN);
                }
                Authenticator.assureEventAdmin(this.dataStore, eventId, currentUser);
            } else {
                Authenticator.assureEditionAdmin(this.dataStore, edition.getId(), currentUser);
            }

            this.dataStore.update(teamRegistration);

        } catch (PersistenceException pe) {
            throw new WebApplicationException("Error writing update" + pe.getMessage(), Status.CONFLICT);
        }

        return true;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/registrations/{eventId}")
    public boolean updateTeamRegistrations(@PathParam("eventId") Integer eventId,
            List<TeamRegistration> teamRegistrations, @Auth @NotNull User currentUser) throws Exception {

        this.checkDataStore();

        try (AutoCloseable c = this.dataStore.getClosable()) {

            TournamentEdition edition = this.dataStore.getEditionByTeamRegistration(teamRegistrations.get(0).getId());

            if (eventId > 0) {
                if (!edition.isAllowEventTeamRegManagement()) {
                    throw new WebApplicationException("You are not allowed to make those changes", Status.FORBIDDEN);
                }
                Authenticator.assureEventAdmin(this.dataStore, eventId, currentUser);
            } else {
                Authenticator.assureEditionAdmin(this.dataStore, edition.getId(), currentUser);
            }

            this.dataStore.updateAll(teamRegistrations);

        } catch (PersistenceException pe) {
            throw new WebApplicationException("Error writing update" + pe.getMessage(), Status.CONFLICT);
        }

        return true;
    }
}
