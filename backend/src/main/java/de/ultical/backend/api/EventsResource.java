package de.ultical.backend.api;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.ibatis.exceptions.PersistenceException;

import de.ultical.backend.app.Authenticator;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.DivisionRegistration;
import de.ultical.backend.model.DivisionRegistrationTeams;
import de.ultical.backend.model.Event;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.User;
import io.dropwizard.auth.Auth;

@Path("/events")
public class EventsResource {

    @Inject
    DataStore dataStore;

    private void checkDatatStore() {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency injection failed!", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Event> getEvents(@QueryParam("from") Date from, @QueryParam("to") Date to) throws Exception {
        this.checkDatatStore();
        try (AutoCloseable c = this.dataStore.getClosable()) {
            return this.dataStore.getEvents(false, from, to);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing databaes failed", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/basics")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Event> getEventsBasics(@QueryParam("from") Date from, @QueryParam("to") Date to) throws Exception {
        this.checkDatatStore();
        try (AutoCloseable c = this.dataStore.getClosable()) {
            return this.dataStore.getEvents(true, from, to);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/{eventId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Event getEvent(@PathParam("eventId") int eventId) {
        this.checkDatatStore();
        try {
            Event result = this.dataStore.get(eventId, Event.class);
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
            Event storedEvent = this.dataStore.addNew(t);
            return storedEvent;
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed!", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{eventId}")
    public void updateEvent(@PathParam("eventId") Integer id, Event event, @Auth @NotNull User currentUser)
            throws Exception {
        this.checkDatatStore();
        if (id.equals(event.getId()) == false) {
            throw new WebApplicationException("Request URL and payload do not match!", Status.NOT_ACCEPTABLE);
        }

        try (AutoCloseable c = this.dataStore.getClosable()) {
            Authenticator.assureEventAdmin(this.dataStore, event.getId(), currentUser);
            boolean updated = this.dataStore.update(event);
            if (!updated) {
                throw new WebApplicationException(
                        "Update failed, eventually someone else update the resource before you", Status.CONFLICT);
            }
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed!", Status.INTERNAL_SERVER_ERROR);
        }
    }

    /*
     * DIVISIONS
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{eventId}/divisions")
    public DivisionRegistration addDivision(@PathParam("eventId") Integer eventId, DivisionRegistration div,
            @Auth @NotNull User currentUser) throws Exception {
        this.checkDatatStore();

        /*
         * we only need the event's id, thus we build a fake-event instead of
         * reading it from the db. If the event does not exist the database's
         * foreign key constraints will fail.
         */
        TournamentEdition fakeEdition = new TournamentEdition();
        fakeEdition.setId(eventId);
        DivisionRegistration storedDiv;
        try (AutoCloseable c = this.dataStore.getClosable()) {
            Authenticator.assureEventAdmin(this.dataStore, eventId, currentUser);
            storedDiv = this.dataStore.addDivisionToEdition(fakeEdition, div);
        } catch (PersistenceException pe) {
            throw new WebApplicationException(pe);
        }
        return storedDiv;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{eventId}/divisions/{divisionId}")
    public void updateDivsion(@PathParam("eventId") Integer eventId, DivisionRegistration div,
            @PathParam("divisionId") Integer divId, @Auth @NotNull User currentUser) throws Exception {
        this.checkDatatStore();
        if (!Integer.valueOf(div.getId()).equals(divId)) {
            throw new WebApplicationException("Request URL and payload do not match!", Status.NOT_ACCEPTABLE);
        }

        try (AutoCloseable c = this.dataStore.getClosable()) {
            Authenticator.assureEventAdmin(this.dataStore, eventId, currentUser);
            final boolean updated = this.dataStore.update(div);
            if (!updated) {
                throw new WebApplicationException(
                        "Update failed, eventually someone else update the resource before you", Status.CONFLICT);
            }
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed!", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("/{eventId}/divisions/{divisionId}")
    public void deleteDivision(@PathParam("divisionId") Integer divId, @Auth @NotNull User currentUser)
            throws Exception {
        this.checkDatatStore();
        try (AutoCloseable c = this.dataStore.getClosable()) {
            Authenticator.assureEventDivisionAdmin(this.dataStore, divId, currentUser);

            DivisionRegistrationTeams fakeDiv = new DivisionRegistrationTeams();
            fakeDiv.setId(divId.intValue());
            this.dataStore.deleteDivision(fakeDiv);
        } catch (PersistenceException pe) {
            throw new WebApplicationException("Accessing database failed!", Status.INTERNAL_SERVER_ERROR);
        }
    }
}
