package de.ultical.backend.api;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.User;
import io.dropwizard.auth.Auth;
import javax.validation.constraints.NotNull;

@Path("/users")
public class UserResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserResource.class);

    @Inject
    DataStore dataStore;

    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getSeasonById(@PathParam("userId") Integer id, @Auth @NotNull User user) {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency Injectino for data store failed!",
                    Status.INTERNAL_SERVER_ERROR);
        }

        User result = null;
        try {
            result = this.dataStore.get(id, User.class);
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
    public List<User> getAllSeasons(@QueryParam("search") String searchString, @Auth @NotNull User user) throws Exception {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency Injectino for data store failed!",
                    Status.INTERNAL_SERVER_ERROR);
        }
        List<User> result = null;
        try (AutoCloseable c = this.dataStore.getClosable()) {
            result = this.dataStore.findUser("%" + searchString + "%");
        } catch (PersistenceException pe) {
            LOGGER.error("Database access failed!", pe);
            throw new WebApplicationException("Accessing the database failed", Status.INTERNAL_SERVER_ERROR);
        }

        // delete personal information
        this.clearPersonalInformation(result);

        return result;
    }

    private void clearPersonalInformation(List<User> users) {
        for (User user : users) {
            this.clearPersonalInformation(user);
        }
    }

    private void clearPersonalInformation(User user) {
        user.setPassword("");
    }

}
