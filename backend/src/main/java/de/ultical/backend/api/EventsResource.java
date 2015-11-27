package de.ultical.backend.api;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Event;

@Path("/events")
public class EventsResource {

	@Inject
	DataStore dStore;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<Event> getAllEvents(@QueryParam("from") Date from, @QueryParam("to") Date to) {
		// I think we should ignore from and to for the moment ;)
		LocalDate ldFrom = from != null ? from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
		LocalDate ldTo = to != null ? to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;

		return this.dStore.getEvents(ldFrom, ldTo);
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public void createNewEvent(Event t) {
		// TODO check authorisation
		this.dStore.storeEvent(t);
	}

}
