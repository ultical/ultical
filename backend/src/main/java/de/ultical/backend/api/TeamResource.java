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
    public List<Team> get(@Auth @NotNull User user) {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }
        List<Team> result = this.dataStore.getTeamsByUser(user.getId());
        return result;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Team add(Team t, @Auth @NotNull User currentUser) {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }

        // Validation
        if (t.getName().length() < 3) {
            throw new WebApplicationException(409);
        }

        t.getAdmins().add(currentUser);

        this.dataStore.setAutoCloseSession(false);

        // Set location to null if undefined
        if (t.getLocation() != null && t.getLocation().getCity() == null) {
            t.setLocation(null);
        } else {
            this.dataStore.addNew(t.getLocation());
        }
        Team result = null;

        try {
            result = this.dataStore.addNew(t);
        } catch (PersistenceException pe) {
            throw new WebApplicationException(pe);
        }

        this.dataStore.closeSession();

        return result;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{teamId}")
    public void update(@PathParam("teamId") Integer id, @Auth @NotNull User currentUser, Team updatedTeam) {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }
        if (!id.equals(updatedTeam.getId())) {
            throw new WebApplicationException(Status.NOT_ACCEPTABLE);
        }
        this.dataStore.setAutoCloseSession(false);
        this.checkAccess(id, currentUser);
        boolean updated = false;
        try {
            updated = this.dataStore.update(updatedTeam);
        } catch (PersistenceException pe) {
            throw new WebApplicationException(pe);
        }
        if (!updated) {
            throw new WebApplicationException(Status.CONFLICT);
        }
    }

    @POST
    @Path("{teamId}/admin/{userId}")
    public void addAdmin(@Auth @NotNull User currentUser, @PathParam("teamId") Integer teamId,
            @PathParam("userId") Integer userId) {
        if (this.dataStore == null) {
            throw new WebApplicationException();
        }
        this.dataStore.setAutoCloseSession(false);
        this.checkAccess(teamId, currentUser);
        final Team team = new Team();
        team.setId(teamId);
        final User admin = new User();
        admin.setId(userId);
        try {
            this.dataStore.setAutoCloseSession(true);
            this.dataStore.addAdminToTeam(team, admin);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing the database failed!");
        }
    }

    @DELETE
    @Path("{teamId}/admin/{userId}")
    public void deleteAdmin(@Auth @NotNull User currentUser, @PathParam("teamId") Integer teamId,
            @PathParam("userId") Integer userId) {
        if (this.dataStore == null) {
            throw new WebApplicationException();
        }
        this.dataStore.setAutoCloseSession(false);
        this.checkAccess(teamId, currentUser);
        final Team fakeTeam = new Team();
        fakeTeam.setId(teamId);
        final User fakeAdmin = new User();
        fakeAdmin.setId(userId);
        try {
            this.dataStore.setAutoCloseSession(true);
            this.dataStore.removeAdminFromTeam(fakeTeam, fakeAdmin);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Acessecing the database failes!");
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
