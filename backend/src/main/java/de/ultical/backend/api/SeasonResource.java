package de.ultical.backend.api;

import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Season;

@Path("/season")
public class SeasonResource {

	private final static Logger LOGGER = LoggerFactory.getLogger(SeasonResource.class);

	@Inject
	DataStore dataStore;

	@GET
	@Path("/{seasonId]")
	@Produces(MediaType.APPLICATION_JSON)
	public Season getSeasonById(@PathParam("seasonId") Integer id) {
		if (this.dataStore == null) {
			throw new WebApplicationException("Dependency Injectino for data store failed!",
					Status.INTERNAL_SERVER_ERROR);
		}
		Season result = null;
		try {
			result = this.dataStore.getSeason(id);
		} catch (PersistenceException pe) {
			LOGGER.error("Database access failed!", pe);
			throw new WebApplicationException("Accessing the database failed", Status.INTERNAL_SERVER_ERROR);
		}
		if (result == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		return result;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Season> getAllSeasons() {
		if (this.dataStore == null) {
			throw new WebApplicationException("Dependency Injectino for data store failed!",
					Status.INTERNAL_SERVER_ERROR);
		}
		List<Season> result = null;
		try {
			result = this.dataStore.getAllSeaoson();
		} catch (PersistenceException pe) {
			LOGGER.error("Database access failed!", pe);
			throw new WebApplicationException("Accessing the database failed", Status.INTERNAL_SERVER_ERROR);
		}
		return result;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Season addSeason(Season newSeason) {
		if (this.dataStore == null) {
			throw new WebApplicationException("Dependency Injectino for data store failed!",
					Status.INTERNAL_SERVER_ERROR);
		}
		Season result = null;
		try {
			result = this.dataStore.addSeason(newSeason);
		} catch (PersistenceException pe) {
			LOGGER.error("Database access failed!", pe);
			throw new WebApplicationException("Accessing the database failed", Status.INTERNAL_SERVER_ERROR);
		}
		return result;
	}

	@PUT
	@Path("/{seasonId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void updateSeason(@PathParam("seasonId") Integer id, @NotNull Season updSeason) {
		if (updSeason.getId() != id) {
			// the id of the season passed as parameter and in the request URL
			// do not match. Evil ...
			throw new WebApplicationException("Request URL and payload do not match!", Status.NOT_ACCEPTABLE);
		}
		if (this.dataStore == null) {
			throw new WebApplicationException("Dependency Injectino for data store failed!",
					Status.INTERNAL_SERVER_ERROR);
		}
		boolean success = false;
		try {
			success = this.dataStore.updateSeason(updSeason);
		} catch (PersistenceException pe) {
			LOGGER.error("Database access failed!", pe);
			throw new WebApplicationException("Accessing the database failed", Status.INTERNAL_SERVER_ERROR);
		}
		if (!success) {
			throw new WebApplicationException("Update failed, eventually someone else update the resource before you",
					Status.CONFLICT);
		}
	}
}
