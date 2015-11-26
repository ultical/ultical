package de.ultical.backend.api;

import java.util.Collection;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.TournamentEdition;

@Path("/tournaments")
public class TournamentResource {

	@Inject
	DataStore dStore;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<TournamentEdition> getAllTournaments() {
		return this.dStore.getAllTournaments();
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

	// @POST
	// @Path("/")
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// public TournamentEdition storeNewTournament(TournamentEdition at) {
	// // TODO check authentication!
	// String exceptionMessage = null;
	// if (at.getName() == null) {
	// exceptionMessage = "a name is required";
	// } /*
	// * else if (at.getEvent() == null) { exceptionMessage =
	// * "at least one Event should be configured"; }
	// */ else if (at.getFirstDay() == null) {
	// exceptionMessage = "at value for firstDay is required";
	// } else if (at.getLastDay() == null) {
	// exceptionMessage = "a value for lastDay is required";
	// }
	// if (exceptionMessage != null) {
	// throw new WebApplicationException(exceptionMessage,
	// Response.Status.BAD_REQUEST);
	// }
	// AbstractTournament existingTournament =
	// this.dStore.getTournamentByName(at.getName());
	// if (existingTournament != null) {
	// throw new WebApplicationException(String.format("A tournament with name:
	// %s already exists", at.getName()),
	// Response.Status.CONFLICT);
	// }
	// this.dStore.storeTournament(at);
	// AbstractTournament result =
	// this.dStore.getTournamentByName(at.getName());
	// return result;
	// }
}
