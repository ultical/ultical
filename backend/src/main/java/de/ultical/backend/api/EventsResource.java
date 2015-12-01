package de.ultical.backend.api;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.joda.time.LocalDate;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Event;

@Path("/events")
public class EventsResource {

	@Inject
	DataStore dStore;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Event> getAllEvents(@QueryParam("from") Date from, @QueryParam("to") Date to) {
		// I think we should ignore from and to for the moment ;)
		LocalDate ldFrom = from == null ? null : new LocalDate(from);
		LocalDate ldTo = to == null ? null : new LocalDate(to);

		return this.dStore.getAllEvents(); // ldFrom, ldTo);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void createNewEvent(Event t) {
		// TODO check authorisation
		this.dStore.storeEvent(t);
	}

}
