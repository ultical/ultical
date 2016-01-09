package de.ultical.backend.api;

import java.util.Collection;
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
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.TournamentEditionSingle;

@Path("/tournaments")
public class TournamentResource {

    @Inject
    DataStore dStore;

    private void checkDataStore() {
        if (this.dStore == null) {
            throw new WebApplicationException("Dependency injection for datastore failed!",
                    Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<? extends TournamentEdition> getAllTournaments() {
        this.checkDataStore();
        try {
            List<? extends TournamentEdition> result = this.dStore.getAll(TournamentEditionSingle.class);
            return result;
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed", pe, Status.INTERNAL_SERVER_ERROR);
        }

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public TournamentEdition storeTournament(final TournamentEditionSingle newEdition) {
        this.checkDataStore();
        try {
            TournamentEdition result = this.dStore.addNew(newEdition);
            return result;
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed", pe, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/{editionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateTournament(final @PathParam("editionId") Integer editionId, TournamentEdition edition) {
        this.checkDataStore();
        try {
            if (editionId.equals(edition.getId()) == false) {
                throw new WebApplicationException("Request URL and payload do not match!", Status.NOT_ACCEPTABLE);
            }
            boolean updated = this.dStore.update(edition);
            if (!updated) {
                throw new WebApplicationException(
                        "Update failed, eventually someone else update the resource before you", Status.CONFLICT);
            }

        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed", pe, Status.INTERNAL_SERVER_ERROR);
        }
    }

}
