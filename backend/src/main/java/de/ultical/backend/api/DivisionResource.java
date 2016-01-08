package de.ultical.backend.api;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.ibatis.exceptions.PersistenceException;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.DivisionRegistrationTeams;
import de.ultical.backend.model.Team;
import de.ultical.backend.model.TeamRegistration;
import de.ultical.backend.model.User;
import io.dropwizard.auth.Auth;

@Path("/divisions")
public class DivisionResource {

    @Inject
    DataStore dStore;

    @POST
    @Path("/{divisionId}/registerTeam/{teamId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void registerTeam(@Auth @NotNull User currentUser, @PathParam("divisionId") Integer divisionId,
            @PathParam("teamId") Integer teamId, TeamRegistration teamReg) {
        if (this.dStore == null) {
            throw new WebApplicationException("Injection DataStore failed!");
        }

        /*
         * If the client provided a team-instance as part of the payload, then
         * we expect that the provided team's id and the request url's teamId
         * are equals. If no team is provided, we create a fake team object that
         * only serves as holder for the id.
         */
        if (teamReg.getTeam() == null) {
            final Team fakeTeam = new Team();
            fakeTeam.setId(teamId);
            teamReg.setTeam(fakeTeam);
        } else {
            final Team receivedTeam = teamReg.getTeam();
            if (!teamId.equals(receivedTeam.getId())) {
                throw new WebApplicationException("Request URL and payload do not match! team-id differs!",
                        Status.NOT_ACCEPTABLE);
            }
        }

        this.dStore.setAutoCloseSession(false);
        Team loadedTeam = this.dStore.get(teamId, Team.class);
        if (loadedTeam == null) {
            throw new WebApplicationException(String.format("A team with id %d does not exist", teamId),
                    Status.NOT_FOUND);
        } else if (!loadedTeam.getAdmins().contains(currentUser)) {
            throw new WebApplicationException(String.format("You are not an admin for team %s", loadedTeam.getName()),
                    Status.FORBIDDEN);
        }

        final DivisionRegistrationTeams divisionReg = new DivisionRegistrationTeams();
        divisionReg.setId(divisionId);
        try {
            this.dStore.setAutoCloseSession(true);
            this.dStore.registerTeamForDivision(divisionReg, teamReg);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing the database failed!", pe);
        }
    }

    @DELETE
    @Path("/{divisionId}/registerTeam/{teamId}")
    public void unregisterTeam(@Auth @NotNull User currentUser, @PathParam("divisionId") Integer divId,
            @PathParam("teamId") Integer teamId) {
        if (this.dStore == null) {
            throw new WebApplicationException("Injection DataStore failed!");
        }
        this.dStore.setAutoCloseSession(false);
        Team teamInDB = this.dStore.get(teamId, Team.class);
        if (teamInDB == null) {
            throw new WebApplicationException(String.format("Team with id %d does not exist", teamId),
                    Status.NOT_FOUND);
        }
        if (!teamInDB.getAdmins().contains(currentUser)) {
            throw new WebApplicationException(String.format("You are not an admin for team %s", teamInDB.getName()),
                    Status.FORBIDDEN);
        }
        final DivisionRegistrationTeams fakeReg = new DivisionRegistrationTeams();
        fakeReg.setId(divId);
        final Team fakeTeam = new Team();
        fakeTeam.setId(teamId);
        try {
            this.dStore.setAutoCloseSession(true);
            this.dStore.unregisterTeamFromDivision(fakeReg, fakeTeam);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed!", pe);
        }
    }
}
