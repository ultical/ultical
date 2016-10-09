package de.ultical.backend.api;

import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import io.dropwizard.auth.Auth;
import javax.ws.rs.Consumes;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Season;
import de.ultical.backend.model.User;
import de.ultical.backend.app.Authenticator;
import de.ultical.backend.exception.AuthorizationException;

@Path("/season")
public class SeasonResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(SeasonResource.class);

    @Inject
    DataStore dataStore;

    @GET
    @Path("/{seasonId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Season getSeasonById(@PathParam("seasonId") Integer id) throws Exception {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency Injectino for data store failed!",
                    Status.INTERNAL_SERVER_ERROR);
        }
        Season result = null;
        try (AutoCloseable c = this.dataStore.getClosable()) {
            result = this.dataStore.get(id, Season.class);
        } catch (PersistenceException pe) {
            LOGGER.error("Database access failed!", pe);
            throw new WebApplicationException("Accessing the database failed", Status.INTERNAL_SERVER_ERROR);
        }
        if (result == null) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        return result;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Season> getAllSeasons() throws Exception {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency Injectino for data store failed!",
                    Status.INTERNAL_SERVER_ERROR);
        }
        try (AutoCloseable c = this.dataStore.getClosable()) {
            return this.dataStore.getAll(Season.class);
        } catch (PersistenceException pe) {
            LOGGER.error("Database access failed!", pe);
            throw new WebApplicationException("Accessing the database failed", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Season addSeason(@Auth @NotNull User currentUser, Season newSeason) throws Exception {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency Injectino for data store failed!",
                    Status.INTERNAL_SERVER_ERROR);
        }
        try (AutoCloseable c = this.dataStore.getClosable()) {
	    Authenticator.assureOverallAdmin(currentUser);
            return this.dataStore.addNew(newSeason);
        } catch (PersistenceException pe) {
            LOGGER.error("Database access failed!", pe);
            throw new WebApplicationException("Accessing the database failed", Status.INTERNAL_SERVER_ERROR);
        } catch (AuthorizationException ae) {
	    LOGGER.warn(String.format("Authorization Issue: User %s (id=%d) tried to create a new season",currentUser.getEmail(), currentUser.getId()), ae);
	    throw new WebApplicationException(Status.UNAUTHORIZED);
	}
    }

    @PUT
    @Path("/{seasonId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateSeason(@PathParam("seasonId") Integer id, @NotNull Season updSeason, @Auth @NotNull User currentUser) throws Exception {
        
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency Injectino for data store failed!",
                    Status.INTERNAL_SERVER_ERROR);
        }
        boolean success = false;
        try (AutoCloseable c = this.dataStore.getClosable()) {
	    Authenticator.assureOverallAdmin(currentUser);
	    if (updSeason.getId() != id) {
		// the id of the season passed as parameter and in the request URL
		// do not match. Evil ...
		throw new WebApplicationException("Request URL and payload do not match!", Status.NOT_ACCEPTABLE);
	    }
            success = this.dataStore.update(updSeason);
        } catch (PersistenceException pe) {
            LOGGER.error("Database access failed!", pe);
            throw new WebApplicationException("Accessing the database failed", Status.INTERNAL_SERVER_ERROR);
        } catch (AuthorizationException ae) {
	    LOGGER.warn(String.format("Authorization Issue: User %s (id=%d) tried to update season", currentUser.getEmail(), currentUser.getId()),ae);
	    throw new WebApplicationException(Status.UNAUTHORIZED);
	}
        if (!success) {
            throw new WebApplicationException("Update failed, eventually someone else update the resource before you",
                    Status.CONFLICT);
        }
    }
}
