package de.ultical.backend.app;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.ibatis.exceptions.PersistenceException;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Event;
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
        boolean isAdmin = false;
        for (User admin : storedTeam.getAdmins()) {
            if (admin.getId() == currentUser.getId()) {
                isAdmin = true;
                break;
            }
        }
        if (!isAdmin) {
            throw new WebApplicationException(String.format("You are not an admin for team %s", storedTeam.getName()),
                    Status.FORBIDDEN);
        }
    }

    public static void assureEventDivisionAdmin(DataStore dataStore, Integer divisionId, User currentUser) {
        Event storedEvent = null;
        try {
            storedEvent = dataStore.getEventByDivision(divisionId);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing the database failed!", Status.INTERNAL_SERVER_ERROR);
        }
        assureEventAdmin(storedEvent, currentUser);
    }

    public static void assureEventAdmin(DataStore dataStore, Integer eventId, User currentUser) {
        Event storedEvent = null;
        try {
            storedEvent = dataStore.get(eventId, Event.class);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing the database failed!", Status.INTERNAL_SERVER_ERROR);
        }
        assureEventAdmin(storedEvent, currentUser);
    }

    public static void assureEventAdmin(Event storedEvent, User currentUser) {
        if (storedEvent == null) {
            throw new WebApplicationException("Event does not exist!", Status.NOT_FOUND);
        }
        List<User> admins = storedEvent.getAdmins();
        admins.addAll(storedEvent.getTournamentEdition().getTournamentFormat().getAdmins());
        boolean isAdmin = false;
        for (User admin : admins) {
            if (admin.getId() == currentUser.getId()) {
                isAdmin = true;
                break;
            }
        }
        if (!isAdmin) {
            throw new WebApplicationException(String.format("You are not an admin for event %d", storedEvent.getId()),
                    Status.FORBIDDEN);
        }
    }
}
