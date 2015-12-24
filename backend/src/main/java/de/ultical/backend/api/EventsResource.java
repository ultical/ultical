package de.ultical.backend.api;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.ibatis.exceptions.PersistenceException;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.*;
import io.dropwizard.auth.Auth;

@Path("/events")
public class EventsResource {

    @Inject
    DataStore dStore;

    private void checkDatatStore() {
        if (this.dStore == null) {
            throw new WebApplicationException("Dependency injection failed!", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Event> getAllEvents(@QueryParam("from") Date from, @QueryParam("to") Date to) {
        // I think we should ignore from and to for the moment ;)
        this.checkDatatStore();
        try {
            return this.dStore.getAll(Event.class);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing databaes failed", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/{eventId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Event getEvent(@PathParam("{eventId}") Integer eventId) {
        this.checkDatatStore();
        try {
            Event result = this.dStore.get(eventId, Event.class);
            if (result == null) {
                throw new WebApplicationException(Status.NOT_FOUND);
            }
            return result;
        } catch (PersistenceException e) {
            throw new WebApplicationException("Accessing database failed!", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Event createNewEvent(Event t, @Auth @NotNull User currentUser) {
        // TODO check authorisation
        this.checkDatatStore();
        try {
            Event storedEvent = this.dStore.addNew(t);
            return storedEvent;
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed!", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{eventId}")
    public void updateEvent(@PathParam("{eventId}") Integer id, Event event, @Auth @NotNull User currentUser) {
        this.checkDatatStore();
        if (id.equals(event.getId()) == false) {
            throw new WebApplicationException("Request URL and payload do not match!", Status.NOT_ACCEPTABLE);
        }
        try {
            boolean updated = this.dStore.update(event);
            if (!updated) {
                throw new WebApplicationException(
                        "Update failed, eventually someone else update the resource before you", Status.CONFLICT);
            }
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed!", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{eventId}/divisions")
    public void addDivision(@PathParam("{eventId}") Integer eventId, DivisionRegistration div,
            @Auth @NotNull User currentUser) {
        this.checkDatatStore();
        /*
         * we only need the event's id, thus we build a fake-event instead of
         * reading it from the db. If the event does not exist the database's
         * foreign key constraints will fail.
         */
        TournamentEdition fakeEdition = new TournamentEditionSingle();
        fakeEdition.setId(eventId);
        try {
            this.dStore.addDivisionToEdition(fakeEdition, div);
        } catch (PersistenceException pe) {
            throw new WebApplicationException(pe);
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{eventId}/divisions")
    public void updateDivsion(@PathParam("{eventId}") Integer eventId, DivisionRegistration div,
            @Auth @NotNull User currentUser) {
        this.checkDatatStore();
        // TODO similar to addDivsion
        throw new WebApplicationException("not implemented, yet!");
    }
}
