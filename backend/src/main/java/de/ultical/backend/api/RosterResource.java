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
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.api.transferClasses.DfvMvPlayer;
import de.ultical.backend.app.Authenticator;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.DivisionType;
import de.ultical.backend.model.Gender;
import de.ultical.backend.model.Player;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.User;
import io.dropwizard.auth.Auth;

@Path("/roster")
public class RosterResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(RosterResource.class);

    @Inject
    private Client client;

    @Inject
    private DataStore dataStore;

    @Inject
    private UltiCalConfig config;

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

        Authenticator.assureTeamAdmin(this.dataStore, newRoster.getTeam().getId(), currentUser);

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

        // Validation
        if (!dfvMvName.isDse()) {
            throw new WebApplicationException("User did not agree to publish his data on the web (no DSE present)",
                    Status.FORBIDDEN);
        }

        this.dataStore.setAutoCloseSession(false);

        Roster roster = this.dataStore.get(new Integer(rosterId), Roster.class);

        Authenticator.assureTeamAdmin(this.dataStore, roster.getTeam().getId(), currentUser);

        // get player if exists
        Player player = this.dataStore.getPlayerByDfvNumber(dfvMvName.getDfvNumber());

        if (player != null) {
            // check if player is already in a roster of this season and
            // division
            Roster result = this.dataStore.getRosterOfPlayerSeason(player.getId(), roster.getSeason().getId(),
                    roster.getDivisionAge().name(), roster.getDivisionType().name());

            if (result != null) {
                // this player is already in a different roster of this season
                throw new WebApplicationException(
                        "e101 - Player is already in a different roster of this season and division", Status.CONFLICT);
            }
        } else {
            // a new player

            // get full player data from dfv-mv
            WebTarget target = this.client.target(this.config.getDfvApi().getUrl()).path("profil")
                    .path(String.valueOf(dfvMvName.getDfvNumber()))
                    .queryParam("token", this.config.getDfvApi().getToken())
                    .queryParam("secret", this.config.getDfvApi().getSecret());

            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
            DfvMvPlayer dfvMvPlayer = invocationBuilder.get(DfvMvPlayer.class);

            // create and persist player object
            DfvPlayer dfvPlayer = new DfvPlayer(dfvMvPlayer);
            dfvPlayer.setFirstName(dfvMvName.getFirstName());
            dfvPlayer.setLastName(dfvMvName.getLastName());
            dfvPlayer.setEmail(dfvMvPlayer.getEmail());

            player = dfvPlayer;

            this.dataStore.storeDfvPlayer(dfvPlayer);
        }

        // check if gender matches with divison
        boolean wrongGender = false;
        if (player.getGender().equals(Gender.MALE)) {
            if (roster.getDivisionType().equals(DivisionType.WOMEN)) {
                wrongGender = true;
            }
        } else if (player.getGender().equals(Gender.NA)) {
            if (roster.getDivisionType().equals(DivisionType.WOMEN)) {
                wrongGender = true;
            }
        }
        if (wrongGender) {
            throw new WebApplicationException("e102 - Player has wrong gender for this Division", Status.CONFLICT);
        }

        // add player to roster
        this.dataStore.addPlayerToRoster(roster, player);

        this.dataStore.closeSession();

        return player;
    }

    @DELETE
    @Path("{rosterId}")
    public void deleteRoster(@Auth @NotNull User currentUser, @PathParam("rosterId") int rosterId) {
        if (this.dataStore == null) {
            throw new WebApplicationException();
        }
        this.dataStore.setAutoCloseSession(false);
        Roster rosterToDelete = this.dataStore.get(rosterId, Roster.class);

        Authenticator.assureTeamAdmin(this.dataStore, rosterToDelete.getTeam().getId(), currentUser);

        try {
            this.dataStore.setAutoCloseSession(true);
            this.dataStore.remove(rosterId, Roster.class);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing the database failes!");
        }
    }
}