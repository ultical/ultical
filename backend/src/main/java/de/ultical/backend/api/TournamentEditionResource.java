package de.ultical.backend.api;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.User;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.List;

@Path("/edition")
public class TournamentEditionResource {

    private static final String LOGIN_MESSAGE = "please login first";
    private static final String UNAUTHORIZED_WARNING = "unauthorized access";
    private static final String DB_ACCESS_FAILED = "database access failed";
    private final static Logger LOGGER = LoggerFactory.getLogger(TournamentEditionResource.class);
    @Inject
    DataStore dataStore;

    @GET
    @Path("/format/{formatId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TournamentEdition> getByFormat(@PathParam("formatId") Integer formatId, @Auth @NotNull User currentUser) {
        this.checkDataStore();

        List<TournamentEdition> result = this.dataStore.getEditionListingByFormat(formatId);
        return result;
    }

    private void checkDataStore() {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency injection failed!", Status.INTERNAL_SERVER_ERROR);
        }
    }
}
