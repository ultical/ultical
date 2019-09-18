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
        this.checkDatatStore();

        assureCompleteEventInformation(event);

        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            TournamentFormat format = dataStore.getFormatByEdition(event.getTournamentEdition().getId());
            Authenticator.assureFormatAdmin(format, currentUser);

            processPreSaveEventDependencies(event);

            try {
                event = this.dataStore.addNew(event);
            } catch (PersistenceException pe) {
                LOG.error(DB_ACCESS_FAILURE, pe);
                throw new WebApplicationException(DB_ACCESS_FAILURE, Status.INTERNAL_SERVER_ERROR);
            }

            Location location = this.dataStore.addNew(event.getLocations().get(0));
            event.getLocations().set(0, location);
            dataStore.addLocationToEvent(event, location);

            processPostSaveEventDependencies(event);

            event.setVersion(1);

            return event;
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

        assureCompleteEventInformation(updatedEvent);

        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            Authenticator.assureEventOrFormatAdmin(dataStore, updatedEvent.getId(), currentUser);

            processPreSaveEventDependencies(updatedEvent);

            boolean updated = this.dataStore.update(updatedEvent);
            if (!updated) {
                throw new WebApplicationException(
                        "Update failed, eventually someone else update the resource before you", Status.CONFLICT);
            }

            this.dataStore.update(updatedEvent.getLocations().get(0));

            processPostSaveEventDependencies(updatedEvent);
        } catch (PersistenceException pe) {
            LOG.error(DB_ACCESS_FAILURE, pe);
            throw new WebApplicationException(DB_ACCESS_FAILURE, Status.INTERNAL_SERVER_ERROR);
        }
    }

    private void processPreSaveEventDependencies(Event event) {
        if (event.getLocalOrganizer() != null) {
            if (event.getLocalOrganizer().getId() == 0) {
                Contact contact = this.dataStore.addNew(event.getLocalOrganizer());
                event.setLocalOrganizer(contact);
            } else {
                this.dataStore.update(event.getLocalOrganizer());
            }
        }
    }

    private void processPostSaveEventDependencies(Event event) {
        // create division confirmation mapping
        // first delete the old mapping
        this.dataStore.removeAllDivisionConfirmationsFromEvent(event);

        for (DivisionConfirmation divCon : event.getDivisionConfirmations()) {
            try {
                this.dataStore.addDivisionConfirmationToEvent(event, divCon);
            } catch (PersistenceException e) {
                LOG.error("exception:", e);
            }
        }

        // create the admin mapping
        // first delete the old mapping
        this.dataStore.removeAllAdminsFromEvent(event);

        for (User admin : event.getAdmins()) {
            try {
                this.dataStore.addAdminToEvent(event, admin);
            } catch (PersistenceException e) {
                LOG.error("Error adding Admin:\nTeam: {} ( {} )\nUser: {} ( {} )\n",
                        event.getName(), event.getId(), admin.getFullName(), admin.getId());
                LOG.error("exception:", e);
            }
        }
    }

    private void assureCompleteEventInformation(Event event) {
        assureNotEmpty(event.getName(), "name");
        assureNotEmpty(event.getStartDate(), "start_date");
        assureNotEmpty(event.getEndDate(), "end_date");
        assureNotEmpty(event.getMatchdayNumber(), "matchday_number");
        assureNotEmpty(event.getLocations(), "location");
        assureNotEmpty(event.getLocations().get(0), "location");
        assureNotEmpty(event.getLocations().get(0).getCity(), "location");
        assureNotEmpty(event.getDivisionConfirmations(), "divisions");
    }

    private void assureNotEmpty(Object obj, String name) {
        boolean empty = false;

        if (obj != null) {
            if (obj instanceof String) {
                empty = ((String) obj).isEmpty();
            } else if (obj instanceof Location) {
                assureNotEmpty(((Location) obj).getCity(), name);
            } else if (obj instanceof List) {
                empty = ((List) obj).isEmpty();
            }
        } else {
            empty = true;
        }
        if (empty) {
            throw new WebApplicationException("Missing event parameters: " + name, Status.EXPECTATION_FAILED);
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
