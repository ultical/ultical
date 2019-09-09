package de.ultical.backend.api;

import de.ultical.backend.app.Authenticator;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.data.DataStore.DataStoreCloseable;
import de.ultical.backend.model.*;
import io.dropwizard.auth.Auth;
import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.Date;
import java.util.List;

@Path("/events")
public class EventsResource {

    private static final String DB_ACCESS_FAILURE = "Accessing database failed";
    private final static Logger LOG = LoggerFactory.getLogger(EventsResource.class);
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
            LOG.error(DB_ACCESS_FAILURE, pe);
            throw new WebApplicationException(DB_ACCESS_FAILURE, Status.INTERNAL_SERVER_ERROR);
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
            LOG.error(DB_ACCESS_FAILURE, pe);
            throw new WebApplicationException(DB_ACCESS_FAILURE, Status.INTERNAL_SERVER_ERROR);
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
            LOG.error(DB_ACCESS_FAILURE, e);
            throw new WebApplicationException(DB_ACCESS_FAILURE, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Event createNewEvent(Event event, @Auth @NotNull User currentUser) {
        // TODO check authorisation
        this.checkDatatStore();
        try {
            Event storedEvent = this.dataStore.addNew(event);
            return storedEvent;
        } catch (PersistenceException pe) {
            LOG.error(DB_ACCESS_FAILURE, pe);
            throw new WebApplicationException(DB_ACCESS_FAILURE, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{eventId}")
    public void updateEvent(@PathParam("eventId") Integer id, Event updatedEvent, @Auth @NotNull User currentUser) {
        this.checkDatatStore();
        if (!id.equals(updatedEvent.getId())) {
            throw new WebApplicationException("Request URL and payload do not match!", Status.NOT_ACCEPTABLE);
        }

        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            Authenticator.assureEventAdmin(this.dataStore, updatedEvent.getId(), currentUser);

            if (updatedEvent.getLocations() == null || updatedEvent.getLocations().get(0) == null
                    || updatedEvent.getLocations().get(0).getCity() == null
                    || updatedEvent.getLocations().get(0).getCity().isEmpty()) {
                throw new WebApplicationException(Status.EXPECTATION_FAILED);
            }

            this.dataStore.update(updatedEvent.getLocations().get(0));

            boolean updated = this.dataStore.update(updatedEvent);
            if (!updated) {
                throw new WebApplicationException(
                        "Update failed, eventually someone else update the resource before you", Status.CONFLICT);
            }
        } catch (PersistenceException pe) {
            LOG.error(DB_ACCESS_FAILURE, pe);
            throw new WebApplicationException(DB_ACCESS_FAILURE, Status.INTERNAL_SERVER_ERROR);
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
            @Auth @NotNull User currentUser) {
        this.checkDatatStore();

        /*
         * we only need the event's id, thus we build a fake-event instead of
         * reading it from the db. If the event does not exist the database's
         * foreign key constraints will fail.
         */
        TournamentEdition fakeEdition = new TournamentEdition();
        fakeEdition.setId(eventId);
        DivisionRegistration storedDiv;
        try (DataStoreCloseable c = this.dataStore.getClosable()) {
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
            @PathParam("divisionId") Integer divId, @Auth @NotNull User currentUser) {
        this.checkDatatStore();
        if (!Integer.valueOf(div.getId()).equals(divId)) {
            throw new WebApplicationException("Request URL and payload do not match!", Status.NOT_ACCEPTABLE);
        }

        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            Authenticator.assureEventAdmin(this.dataStore, eventId, currentUser);
            final boolean updated = this.dataStore.update(div);
            if (!updated) {
                throw new WebApplicationException(
                        "Update failed, eventually someone else update the resource before you", Status.CONFLICT);
            }
        } catch (PersistenceException pe) {
	    LOG.error(DB_ACCESS_FAILURE, pe);
            throw new WebApplicationException("Accessing database failed!", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("/{eventId}/divisions/{divisionId}")
    public void deleteDivision(@PathParam("divisionId") Integer divId, @Auth @NotNull User currentUser) {
        this.checkDatatStore();
        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            Authenticator.assureEventDivisionAdmin(this.dataStore, divId, currentUser);

            DivisionRegistrationTeams fakeDiv = new DivisionRegistrationTeams();
            fakeDiv.setId(divId.intValue());
            this.dataStore.deleteDivision(fakeDiv);
        } catch (PersistenceException pe) {
	    LOG.error(DB_ACCESS_FAILURE, pe);
            throw new WebApplicationException("Accessing database failed!", Status.INTERNAL_SERVER_ERROR);
        }
    }
}
