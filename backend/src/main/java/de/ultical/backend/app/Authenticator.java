package de.ultical.backend.app;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.ibatis.exceptions.PersistenceException;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.exception.AuthorizationException;
import de.ultical.backend.model.Event;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.Team;
import de.ultical.backend.model.TournamentFormat;
import de.ultical.backend.model.User;

public class Authenticator {

    private static Set<String> overallAdmins;

    public static boolean addAdmin(final String adminEmail) {
        boolean result = false;
        if (adminEmail != null) {
            if (Authenticator.overallAdmins == null) {
                Authenticator.overallAdmins = new HashSet<>();
            }
            result = Authenticator.overallAdmins.add(adminEmail);
        }
        return result;
    }

    public static void assureRosterAdmin(DataStore dataStore, Integer rosterId, User currentUser) {
        Roster storedRoster = null;
        try {
            storedRoster = dataStore.get(rosterId, Roster.class);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing the database failed!", pe, Status.INTERNAL_SERVER_ERROR);
        }
        if (storedRoster == null) {
            throw new WebApplicationException(String.format("Roster with id %d does not exist!", rosterId),
                    Status.NOT_FOUND);
        }
        checkTeamAdmin(storedRoster.getTeam(), currentUser);
    }

    public static void assureTeamAdmin(DataStore dataStore, Integer teamId, User currentUser) {
        Team storedTeam = null;
        try {
            storedTeam = dataStore.get(teamId, Team.class);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing the database failed!", pe, Status.INTERNAL_SERVER_ERROR);
        }
        if (storedTeam == null) {
            throw new WebApplicationException(String.format("Team with id %d does not exist!", teamId),
                    Status.NOT_FOUND);
        }
        checkTeamAdmin(storedTeam, currentUser);
    }

    public static void checkTeamAdmin(Team team, User currentUser) {
        boolean isAdmin = false;
        for (User admin : team.getAdmins()) {
            if (admin.getId() == currentUser.getId()) {
                isAdmin = true;
                break;
            }
        }
        if (!isAdmin) {
            throw new WebApplicationException(String.format("You are not an admin for team %s", team.getName()),
                    Status.FORBIDDEN);
        }
    }

    public static void assureEventDivisionAdmin(DataStore dataStore, Integer divisionId, User currentUser) {
        Event storedEvent = null;
        try {
            storedEvent = dataStore.getEventByDivision(divisionId);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing the database failed!", pe, Status.INTERNAL_SERVER_ERROR);
        }
        assureEventAdmin(storedEvent, currentUser);
    }

    public static void assureEventAdmin(DataStore dataStore, Integer eventId, User currentUser) {
        Event storedEvent = null;
        try {
            storedEvent = dataStore.get(eventId, Event.class);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing the database failed!", pe, Status.INTERNAL_SERVER_ERROR);
        }
        assureEventAdmin(storedEvent, currentUser);
    }

    public static void assureEventAdmin(Event storedEvent, User currentUser) {
        if (storedEvent == null) {
            throw new WebApplicationException("Event does not exist!", Status.NOT_FOUND);
        }
        boolean isAdmin = isEventAdmin(storedEvent, currentUser);

        if (!isAdmin) {
            throw new WebApplicationException(String.format("You are not an admin for event %d", storedEvent.getId()),
                    Status.FORBIDDEN);
        }
    }

    public static boolean isEventAdmin(Event storedEvent, User currentUser) {
        List<User> admins = storedEvent.getAdmins();
        admins.addAll(storedEvent.getTournamentEdition().getTournamentFormat().getAdmins());
        boolean isAdmin = false;
        for (User admin : admins) {
            if (admin.getId() == currentUser.getId()) {
                isAdmin = true;
                break;
            }
        }
        return isAdmin;
    }

    public static void assureEventOrFormatAdmin(DataStore dataStore, int eventId, User user) {
        //TournamentFormat format = dataStore.getFormatByEvent(eventId);
        Event event = dataStore.getEvent(eventId);

        boolean isEventAdmin = isEventAdmin(event, user);

        TournamentFormat format = event.getTournamentEdition().getTournamentFormat();
        boolean isFormatAdmin = isFormatAdmin(format, user);

        if (!isEventAdmin && !isFormatAdmin) {
            throw new WebApplicationException(
                    String.format("You are neither an admin for format %d, nor for event %d", format.getId(), event.getId()),
                    Status.FORBIDDEN);
        }
    }

    public static void assureEditionAdmin(DataStore dataStore, Integer editionId, User currentUser) {
        TournamentFormat storedFormat = null;
        try {
            storedFormat = dataStore.getFormatByEdition(editionId);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing the database failed!", pe, Status.INTERNAL_SERVER_ERROR);
        }
        assureFormatAdmin(storedFormat, currentUser);
    }

    public static void assureFormatAdmin(TournamentFormat storedFormat, User currentUser) {
        if (storedFormat == null) {
            throw new WebApplicationException("Format does not exist!", Status.NOT_FOUND);
        }
        boolean isAdmin = isFormatAdmin(storedFormat, currentUser);

        if (!isAdmin) {
            throw new WebApplicationException(String.format("You are not an admin for format %d", storedFormat.getId()),
                    Status.FORBIDDEN);
        }
    }

    public static boolean isFormatAdmin(TournamentFormat storedFormat, User currentUser) {
        List<User> admins = storedFormat.getAdmins();
        boolean isAdmin = false;
        for (User admin : admins) {
            if (admin.getId() == currentUser.getId()) {
                isAdmin = true;
                break;
            }
        }
        return isAdmin;
    }

    public static void assureOverallAdmin(final User user) {
        Objects.requireNonNull(user);
        if (Authenticator.overallAdmins == null || !Authenticator.overallAdmins.contains(user.getEmail())) {
            throw new AuthorizationException(
                    String.format("User %s (id=%d) is not authorized as overall admin", user.getEmail(), user.getId()));
        }

    }
}
