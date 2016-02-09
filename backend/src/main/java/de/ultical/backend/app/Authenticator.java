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
