package de.ultical.backend.api;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.ibatis.exceptions.PersistenceException;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Team;

@Path("/teams")
public class TeamResource {

	@Inject
	DataStore dataStore;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Team> getAll() {
		if (this.dataStore == null) {
			throw new WebApplicationException(500);
		}
		List<Team> result = this.dataStore.getAll(Team.class);
		return result;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{teamId}")
	public Team get(@PathParam("{teamId}") Integer id) {
		if (this.dataStore == null) {
			throw new WebApplicationException(500);
		}
		Team result = this.dataStore.get(id, Team.class);
		if (result == null) {
			throw new WebApplicationException(404);
		}
		return result;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Team add(Team t) {
		if (this.dataStore == null) {
			throw new WebApplicationException(500);
		}
		Team result = null;
		try {
			result = this.dataStore.addNew(t);
		} catch (PersistenceException pe) {
			throw new WebApplicationException(pe);
		}
		return result;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{teamId}")
	public void update(@PathParam("{teamId}") Integer id, Team updatedTeam) {
		if (this.dataStore == null) {
			throw new WebApplicationException(500);
		}
		if (!id.equals(updatedTeam.getId())) {
			throw new WebApplicationException(Status.NOT_ACCEPTABLE);
		}
		boolean updated = false;
		try {
			updated = this.dataStore.update(updatedTeam);
		} catch (PersistenceException pe) {
			throw new WebApplicationException(pe);
		}
		if (!updated) {
			throw new WebApplicationException(Status.CONFLICT);
		}
	}
}
