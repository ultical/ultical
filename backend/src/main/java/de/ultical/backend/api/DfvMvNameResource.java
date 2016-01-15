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
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.api.transferClasses.DfvMvPlayer;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.User;
import io.dropwizard.auth.Auth;

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
    public List<DfvMvName> findDfvMvName(@QueryParam("search") String searchString, @Auth User user) {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency Injection for data store failed!",
                    Status.INTERNAL_SERVER_ERROR);
        }
        List<DfvMvName> result = null;
        this.dataStore.setAutoCloseSession(false);

        try {
            result = this.dataStore.findDfvMvName("%" + searchString + "%");

            // get find duplicates
            List<DfvMvName> duplicates = new ArrayList<DfvMvName>();
            if (result.size() <= 5) {
                for (DfvMvName name : result) {
                    for (DfvMvName name2 : result) {
                        if (name.getFirstName().equalsIgnoreCase(name2.getFirstName())
                                && name.getLastName().equalsIgnoreCase(name2.getLastName()) && name != name2) {
                            duplicates.add(name);
                        }
                    }
                }
            }
            for (DfvMvName name : duplicates) {
                WebTarget target = this.client.target(this.config.getDfvApi().getUrl()).path("profil")
                        .path(String.valueOf(name.getDfvNumber()))
                        .queryParam("token", this.config.getDfvApi().getToken())
                        .queryParam("secret", this.config.getDfvApi().getSecret());

                Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
                DfvMvPlayer player = invocationBuilder.get(DfvMvPlayer.class);

                name.setClub(this.dataStore.getClub(player.getVerein()).getName());
            }

        } catch (PersistenceException pe) {
            LOGGER.error("Database access failed!", pe);
            throw new WebApplicationException("Accessing the database failed", Status.INTERNAL_SERVER_ERROR);
        } finally {
            this.dataStore.closeSession();
        }
        return result;
    }

}
