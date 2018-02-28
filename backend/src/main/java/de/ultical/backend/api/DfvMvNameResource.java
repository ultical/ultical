package de.ultical.backend.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.data.DataStore.DataStoreCloseable;
import de.ultical.backend.model.User;
import io.dropwizard.auth.Auth;
import org.apache.ibatis.exceptions.PersistenceException;

@Path("/dfvmvname")
public class DfvMvNameResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(DfvMvNameResource.class);

    @Inject
    DataStore dataStore;

    @Inject
    Client client;

    @Inject
    UltiCalConfig config;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<DfvMvName> findDfvMvName(@QueryParam("search") String searchStringRaw, @Auth User user) {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency Injection for data store failed!",
                    Status.INTERNAL_SERVER_ERROR);
        }
        List<DfvMvName> result = null;

        // split search string at the spaces and dashes
        List<String> searchStrings = new ArrayList<String>();
        for (String searchString : searchStringRaw.split("\\s|-")) {
            searchStrings.add("%" + searchString + "%");
        }

        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            result = this.dataStore.findDfvMvName(searchStrings);
        } catch (PersistenceException pe) {
            LOGGER.error("Database access failed!", pe);
            throw new WebApplicationException("Accessing the database failed", Status.INTERNAL_SERVER_ERROR);
        }
        return result;
    }

}
