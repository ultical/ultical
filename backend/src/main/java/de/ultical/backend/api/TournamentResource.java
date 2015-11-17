package de.ultical.backend.api;

import java.util.Collection;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.AbstractTournament;
import de.ultical.backend.model.Tournament;

@Path("/tournaments")
public class TournamentResource {

	@Inject
	DataStore dStore;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	Collection<Tournament> getAllTournaments() {
		return dStore.getAllTournaments();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{tName}")
	public AbstractTournament getTournamentByName(@PathParam("tName") String tName) {
		AbstractTournament result = this.dStore.getTournamentByName(tName);
		if (result == null) {
			throw new WebApplicationException(String.format("Tournament with name %s could not be found", tName),
					Response.Status.NOT_FOUND);
		}
		return result;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public AbstractTournament storeNewTournament(Tournament at) {
		// TODO check authentication!
		String exceptionMessage = null;
		if (at.getName() == null) {
			exceptionMessage = "a name is required";
		} else if (at.getEvent() == null) {
			exceptionMessage = "at least one Event should be configured";
		} else if (at.getFirstDay() == null) {
			exceptionMessage = "at value for firstDay is required";
		} else if (at.getLastDay() == null) {
			exceptionMessage = "a value for lastDay is required";
		}
		if (exceptionMessage != null) {
			throw new WebApplicationException(exceptionMessage, Response.Status.BAD_REQUEST);
		}
		AbstractTournament existingTournament = this.dStore.getTournamentByName(at.getName());
		if (existingTournament != null) {
			throw new WebApplicationException(String.format("A tournament with name: %s already exists", at.getName()),
					Response.Status.CONFLICT);
		}
		this.dStore.storeTournament(at);
		AbstractTournament result = this.dStore.getTournamentByName(at.getName());
		return result;
	}
}
