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
import javax.ws.rs.DELETE;
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
import de.ultical.backend.model.Team;
import de.ultical.backend.model.User;
import io.dropwizard.auth.Auth;

@Path("/teams")
public class TeamResource {

    @Inject
    DataStore dataStore;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Team> getAll() {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }
        List<Team> result = this.dataStore.getAll(Team.class);
        return result;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{teamId}")
    public Team get(@PathParam("teamId") Integer id) {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }
        Team result = this.dataStore.get(id, Team.class);
        if (result == null) {
            throw new WebApplicationException(404);
        }
        return result;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("own")
    public List<Team> get(@Auth @NotNull User user) throws Exception {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }

        try (AutoCloseable c = this.dataStore.getClosable()) {
            return this.dataStore.getTeamsByUser(user.getId());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Team add(Team t, @Auth @NotNull User currentUser) throws Exception {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }

        try (AutoCloseable c = this.dataStore.getClosable()) {

            t = this.prepareTeam(t);

            if (t.getLocation() == null || t.getLocation().getCity() == null || t.getLocation().getCity().isEmpty()) {
                throw new WebApplicationException("Location must be specified", Status.EXPECTATION_FAILED);
                // t.setLocation(new Location());
            }

            this.dataStore.addNew(t.getLocation());

            Team result = null;

            try {
                result = this.dataStore.addNew(t);
            } catch (PersistenceException pe) {
                throw new WebApplicationException(pe);
            }

            // add admins
            for (User admin : t.getAdmins()) {
                this.dataStore.addAdminToTeam(result, admin);
            }

            return result;
        }
    }

    private Team prepareTeam(Team t) {
        // Validation
        if (t.getName().length() < 3) {
            throw new WebApplicationException("Teamname must be at least 2 characters", Status.LENGTH_REQUIRED);
        }

        // check if a team with the same name already exists
        Team checkTeam = this.dataStore.getTeamByName(t.getName());
        if (checkTeam != null && checkTeam.getId() != t.getId()) {
            throw new WebApplicationException("Teamname already taken", Status.CONFLICT);
        }

        return t;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{teamId}")
    public void update(Team updatedTeam, @PathParam("teamId") Integer teamId, @Auth @NotNull User currentUser)
            throws Exception {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }

        try (AutoCloseable c = this.dataStore.getClosable()) {

            Authenticator.assureTeamAdmin(this.dataStore, teamId, currentUser);

            if (!teamId.equals(updatedTeam.getId())) {
                throw new WebApplicationException(Status.NOT_ACCEPTABLE);
            }

            updatedTeam = this.prepareTeam(updatedTeam);

            if (updatedTeam.getLocation() == null || updatedTeam.getLocation().getCity() == null
                    || updatedTeam.getLocation().getCity().isEmpty()) {
                throw new WebApplicationException(Status.EXPECTATION_FAILED);
            }

            this.dataStore.update(updatedTeam.getLocation());

            boolean updated = false;
            try {
                updated = this.dataStore.update(updatedTeam);
            } catch (PersistenceException pe) {
                throw new WebApplicationException(pe);
            }
            if (!updated) {
                throw new WebApplicationException(Status.CONFLICT);
            }

            // create the admin mapping
            // first delete the old mapping
            this.dataStore.removeAllAdminsFromTeam(updatedTeam);

            for (User admin : updatedTeam.getAdmins()) {
                this.dataStore.addAdminToTeam(updatedTeam, admin);
            }
        }
    }

    @POST
    @Path("{teamId}/admin/{userId}")
    public void addAdmin(@Auth @NotNull User currentUser, @PathParam("teamId") Integer teamId,
            @PathParam("userId") Integer userId) throws Exception {
        if (this.dataStore == null) {
            throw new WebApplicationException();
        }
        try (AutoCloseable c = this.dataStore.getClosable()) {
            Authenticator.assureTeamAdmin(this.dataStore, teamId, currentUser);
            final Team team = new Team();
            team.setId(teamId);
            final User admin = new User();
            admin.setId(userId);
            try {

                this.dataStore.addAdminToTeam(team, admin);
            } catch (PersistenceException pe) {
                throw new WebApplicationException("Accessing the database failed!");
            }
        }
    }

    @DELETE
    @Path("{teamId}/admin/{userId}")
    public void deleteAdmin(@Auth @NotNull User currentUser, @PathParam("teamId") Integer teamId,
            @PathParam("userId") Integer userId) throws Exception {
        if (this.dataStore == null) {
            throw new WebApplicationException();
        }
        try (AutoCloseable c = this.dataStore.getClosable()) {

            Authenticator.assureTeamAdmin(this.dataStore, teamId, currentUser);

            final Team fakeTeam = new Team();
            fakeTeam.setId(teamId);
            final User fakeAdmin = new User();
            fakeAdmin.setId(userId);
            try {

                this.dataStore.removeAdminFromTeam(fakeTeam, fakeAdmin);
            } catch (PersistenceException pe) {
                throw new WebApplicationException("Acessecing the database failes!");
            }
        }
    }
}
