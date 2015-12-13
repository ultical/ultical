package de.ultical.backend.api;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
		checkDataStore();
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
		checkDataStore();
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
	public void updateTournament(final @PathParam("{editionId}") Integer editionId, TournamentEdition edition) {
		checkDataStore();
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

	@GET
	@Path("/{tName}")
	@Produces(MediaType.APPLICATION_JSON)
	public TournamentEdition getTournamentByName(@PathParam("tName") String tName) {
		TournamentEdition result = this.dStore.getTournamentByName(tName);
		if (result == null) {
			throw new WebApplicationException(String.format("Tournament with name %s could not be found", tName),
					Response.Status.NOT_FOUND);
		}
		return result;
	}

}
