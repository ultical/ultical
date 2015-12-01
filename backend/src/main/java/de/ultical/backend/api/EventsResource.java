package de.ultical.backend.api;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Event;

@Path("/events")
public class EventsResource {

	@Inject
	DataStore dStore;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Event> getAllEvents(@QueryParam("from") LocalDate from, @QueryParam("to") LocalDate to) {
		// I think we should ignore from and to for the moment ;)

		return this.dStore.getAllEvents(from, to);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void createNewEvent(Event t) {
		// TODO check authorisation
		this.dStore.storeEvent(t);
	}

}
