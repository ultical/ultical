package de.ultical.backend.api;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Player;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.Team;
import de.ultical.backend.model.User;
import io.dropwizard.auth.Auth;

@Path("/roster")
public class RosterResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(RosterResource.class);

    @Inject
    DataStore dataStore;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Roster addRoster(@Auth User currentUser, @NotNull Roster newRoster) {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency Injectino for data store failed!",
                    Status.INTERNAL_SERVER_ERROR);
        }

        Roster result = null;

        this.dataStore.setAutoCloseSession(false);

        this.checkAccess(newRoster.getTeam().getId(), currentUser);

        try {
            this.dataStore.addNew(newRoster);
        } catch (PersistenceException pe) {
            LOGGER.error("Database access failed!", pe);
            throw new WebApplicationException("Accessing the database failed", Status.INTERNAL_SERVER_ERROR);
        }

        this.dataStore.closeSession();

        return result;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{rosterId}")
    public Player addPlayerToRoster(@Auth User currentUser, @PathParam("rosterId") Integer rosterId,
            @NotNull DfvMvName dfvMvName) {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency Injection for data store failed!",
                    Status.INTERNAL_SERVER_ERROR);
        }

        this.dataStore.setAutoCloseSession(false);

        Roster roster = this.dataStore.get(new Integer(rosterId), Roster.class);

        this.checkAccess(roster.getTeam().getId(), currentUser);

        // get full player data from dfv-mv

        // create and persist player object

        // add player to roster

        Player result = null;

        // try {
        // this.dataStore.addNew(roster, roster.class);
        // } catch (PersistenceException pe) {
        // LOGGER.error("Database access failed!", pe);
        // throw new WebApplicationException("Accessing the database failed",
        // Status.INTERNAL_SERVER_ERROR);
        // }

        this.dataStore.closeSession();

        return result;
    }

    @DELETE
    @Path("{rosterId}")
    public void deleteRoster(@Auth @NotNull User currentUser, @PathParam("rosterId") int rosterId) {
        if (this.dataStore == null) {
            throw new WebApplicationException();
        }
        this.dataStore.setAutoCloseSession(false);
        Roster rosterToDelete = this.dataStore.get(rosterId, Roster.class);

        this.checkAccess(rosterToDelete.getTeam().getId(), currentUser);

        try {
            this.dataStore.setAutoCloseSession(true);
            this.dataStore.remove(rosterId, Roster.class);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing the database failes!");
        }
    }

    private void checkAccess(Integer id, User currentUser) {
        Team storedTeam = null;
        try {
            storedTeam = this.dataStore.get(id, Team.class);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing the database failed!", Status.INTERNAL_SERVER_ERROR);
        }
        if (storedTeam == null) {
            throw new WebApplicationException(String.format("Team with id %d does not exist!", id), Status.NOT_FOUND);
        }
        if (!storedTeam.getAdmins().contains(currentUser)) {
            throw new WebApplicationException(String.format("You are not an admin for team %s", storedTeam.getName()),
                    Status.FORBIDDEN);
        }
    }
}
