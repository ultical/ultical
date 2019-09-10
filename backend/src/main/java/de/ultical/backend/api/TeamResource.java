package de.ultical.backend.api;

import de.ultical.backend.app.Authenticator;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.data.DataStore.DataStoreCloseable;
import de.ultical.backend.model.Location;
import de.ultical.backend.model.Team;
import de.ultical.backend.model.User;
import io.dropwizard.auth.Auth;
import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.List;

@Path("/teams")
public class TeamResource {

    private static final String DB_ACCESS_FAILED = "database access failed";

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamResource.class);

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
    @Path("basics")
    public List<Team> getBasics()  {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }

        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            return this.dataStore.getTeamBasics();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{teamId}")
    public Team get(@PathParam("teamId") Integer id)  {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }

        Team result;

        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            result = this.dataStore.get(id, Team.class);
            if (result == null) {
                throw new WebApplicationException(404);
            }
        }
        return result;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("own")
    public List<Team> get(@Auth @NotNull User user)  {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }

        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            return this.dataStore.getTeamsByUser(user.getId());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("own/basics")
    public List<Team> getBasicsByUser(@Auth @NotNull User user)  {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }

        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            return this.dataStore.getTeamBasicsByUser(user.getId());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Team add(final Team newTeam, @Auth @NotNull User currentUser)  {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }

        try (DataStoreCloseable c = this.dataStore.getClosable()) {
	    
            Team preparedTeam = this.prepareTeam(newTeam);

            if (preparedTeam.getLocation() == null || preparedTeam.getLocation().getCity() == null
                    || preparedTeam.getLocation().getCity().isEmpty()) {
                throw new WebApplicationException("Location must be specified", Status.EXPECTATION_FAILED);
            }

            this.dataStore.addNew(newTeam.getLocation());

            try {
                preparedTeam = this.dataStore.addNew(preparedTeam);
            } catch (PersistenceException pe) {
                LOGGER.error(DB_ACCESS_FAILED, pe);
                throw new WebApplicationException(pe);
            }

            // add admins
            for (User admin : preparedTeam.getAdmins()) {
                try {
                    this.dataStore.addAdminToTeam(preparedTeam, admin);
                } catch (PersistenceException e) {
                    LOGGER.error("Error adding Admin:\nTeam: {} ( {} )\nUser: {} ( {} )", preparedTeam.getName(),
                            preparedTeam.getId(), admin.getFullName(), admin.getId());
                    LOGGER.error("exception:", e);
                }
            }

            preparedTeam.setVersion(1);

            return preparedTeam;
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
             {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }

        try (DataStoreCloseable c = this.dataStore.getClosable()) {

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
                LOGGER.error(DB_ACCESS_FAILED, pe);
                throw new WebApplicationException(pe);
            }
            if (!updated) {
                throw new WebApplicationException(Status.CONFLICT);
            }

            // create the admin mapping
            // first delete the old mapping
            this.dataStore.removeAllAdminsFromTeam(updatedTeam);

            for (User admin : updatedTeam.getAdmins()) {
                try {
                    this.dataStore.addAdminToTeam(updatedTeam, admin);
                } catch (PersistenceException e) {
                    LOGGER.error("Error adding Admin:\nTeam: {} ( {} )\nUser: {} ( {} )\ncurrentUser: {}",
                            updatedTeam.getName(), updatedTeam.getId(), admin.getFullName(), admin.getId(),
                            currentUser.getId());
                    LOGGER.error("exception:", e);
                }
            }
        }
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{teamId}")
    public void delete(@PathParam("teamId") Integer teamId, @Auth @NotNull User currentUser)  {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }

        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            Authenticator.assureTeamAdmin(this.dataStore, teamId, currentUser);
            Team teamToDelete = this.dataStore.get(teamId, Team.class);
            this.dataStore.remove(teamToDelete.getLocation().getId(), Location.class);
        } catch (PersistenceException pe) {
            LOGGER.error(DB_ACCESS_FAILED, pe);
            throw new WebApplicationException("c17 - Deletion not successful!", Status.CONFLICT);
        }
    }

    @POST
    @Path("{teamId}/admin/{userId}")
    public void addAdmin(@Auth @NotNull User currentUser, @PathParam("teamId") Integer teamId,
            @PathParam("userId") Integer userId)  {
        if (this.dataStore == null) {
            throw new WebApplicationException();
        }
        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            Authenticator.assureTeamAdmin(this.dataStore, teamId, currentUser);
            final Team team = new Team();
            team.setId(teamId);
            final User admin = new User();
            admin.setId(userId);

            try {
                this.dataStore.addAdminToTeam(team, admin);
            } catch (PersistenceException pe) {
                LOGGER.error(DB_ACCESS_FAILED, pe);
                throw new WebApplicationException(DB_ACCESS_FAILED);
            }
        }
    }

    @DELETE
    @Path("{teamId}/admin/{userId}")
    public void deleteAdmin(@Auth @NotNull User currentUser, @PathParam("teamId") Integer teamId,
            @PathParam("userId") Integer userId)  {
        if (this.dataStore == null) {
            throw new WebApplicationException();
        }
        try (DataStoreCloseable c = this.dataStore.getClosable()) {

            Authenticator.assureTeamAdmin(this.dataStore, teamId, currentUser);

            final Team fakeTeam = new Team();
            fakeTeam.setId(teamId);
            final User fakeAdmin = new User();
            fakeAdmin.setId(userId);
            try {
                this.dataStore.removeAdminFromTeam(fakeTeam, fakeAdmin);
            } catch (PersistenceException pe) {
                LOGGER.error(DB_ACCESS_FAILED, pe);
                throw new WebApplicationException(DB_ACCESS_FAILED);
            }
        }
    }
}
