package de.ultical.backend.api;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.ibatis.exceptions.PersistenceException;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Club;

/**
 * Handle clubs
 *
 * @author bas
 *
 */
@Path("/club")
public class ClubResource {

    @Inject
    DataStore dataStore;

    @GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Club> getAllClubs() throws Exception {

        try (AutoCloseable c = this.dataStore.getClosable()) {
            return this.dataStore.getAllClubs();
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed - ClubResource - " + pe.getMessage(),
                    Status.INTERNAL_SERVER_ERROR);
        }
    }

}
