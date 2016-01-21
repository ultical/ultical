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
import de.ultical.backend.model.Location;
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

        this.dataStore.setAutoCloseSession(false);

        t = this.prepareTeam(t);

        if (t.getLocation() == null) {
            t.setLocation(new Location());
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

        this.dataStore.closeSession();

        return result;
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
    public void update(Team updatedTeam, @PathParam("teamId") Integer teamId, @Auth @NotNull User currentUser) {
        if (this.dataStore == null) {
            throw new WebApplicationException(500);
        }

        this.dataStore.setAutoCloseSession(false);

        Authenticator.assureTeamAdmin(this.dataStore, teamId, currentUser);

        if (!teamId.equals(updatedTeam.getId())) {
            throw new WebApplicationException(Status.NOT_ACCEPTABLE);
        }

        updatedTeam = this.prepareTeam(updatedTeam);

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

        this.dataStore.closeSession();

    }

    @POST
    @Path("{teamId}/admin/{userId}")
    public void addAdmin(@Auth @NotNull User currentUser, @PathParam("teamId") Integer teamId,
            @PathParam("userId") Integer userId) {
        if (this.dataStore == null) {
            throw new WebApplicationException();
        }
        this.dataStore.setAutoCloseSession(false);
        Authenticator.assureTeamAdmin(this.dataStore, teamId, currentUser);
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

        Authenticator.assureTeamAdmin(this.dataStore, teamId, currentUser);

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
}
