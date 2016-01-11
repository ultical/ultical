package de.ultical.backend.app;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.ibatis.exceptions.PersistenceException;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Team;
import de.ultical.backend.model.User;

public class Authenticator {

    public static void assureTeamAdmin(DataStore dataStore, Integer teamId, User currentUser) {
        Team storedTeam = null;
        try {
            storedTeam = dataStore.get(teamId, Team.class);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing the database failed!", Status.INTERNAL_SERVER_ERROR);
        }
        if (storedTeam == null) {
            throw new WebApplicationException(String.format("Team with id %d does not exist!", teamId),
                    Status.NOT_FOUND);
        }
        if (!storedTeam.getAdmins().contains(currentUser)) {
            throw new WebApplicationException(String.format("You are not an admin for team %s", storedTeam.getName()),
                    Status.FORBIDDEN);
        }
    }
}
