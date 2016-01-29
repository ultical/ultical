package de.ultical.backend.api;

import java.util.List;

import javax.inject.Inject;
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

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.TournamentFormat;

@Path("/format")
public class TournamentFormatResource {

    @Inject
    DataStore dataStore;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TournamentFormat> getAll() {
        this.checkDataStore();
        List<TournamentFormat> result = this.dataStore.getAll(TournamentFormat.class);
        return result;
    }

    private void checkDataStore() {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency injection failed!", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TournamentFormat addNewFormat(TournamentFormat tf) {
        this.checkDataStore();
        try {
            TournamentFormat result = this.dataStore.addNew(tf);
            return result;
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed!", pe,
                    Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    @PUT
    @Path("/{formatId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateFormat(@PathParam("formatId") Integer formatId, TournamentFormat updatedFormat) {
        this.checkDataStore();
        try {
            if (formatId.equals(updatedFormat.getId()) == false) {
                throw new WebApplicationException("Request URL and payload do not match!", Status.NOT_ACCEPTABLE);
            }
            final boolean update = this.dataStore.update(updatedFormat);
            if (!update) {
                throw new WebApplicationException("Update failed, eventually someone else updated in the meantime",
                        Status.CONFLICT);
            }
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed!", pe,
                    Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    @GET
    @Path("/{formatId}")
    @Produces(MediaType.APPLICATION_JSON)
    public TournamentFormat getFormat(@PathParam("formatId") Integer formatId) {
        this.checkDataStore();
        try {
            TournamentFormat result = this.dataStore.get(formatId, TournamentFormat.class);
            if (result == null) {
                throw new WebApplicationException(Status.NOT_FOUND);
            }
            return result;
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed!", pe,
                    Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    @GET
    @Path("/event/{eventId}")
    @Produces(MediaType.APPLICATION_JSON)
    public TournamentFormat getFormatByEvent(@PathParam("eventId") Integer eventId) throws Exception {
        this.checkDataStore();

        try (AutoCloseable c = this.dataStore.getClosable()) {

            TournamentFormat result = this.dataStore.getFormatByEvent(eventId);
            if (result == null) {
                throw new WebApplicationException(Status.NOT_FOUND);
            }
            return result;
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed!", pe,
                    Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }
}
