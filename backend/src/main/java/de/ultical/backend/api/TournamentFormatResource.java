package de.ultical.backend.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import de.ultical.backend.data.mapper.TournamentFormatMapper;
import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ultical.backend.app.Authenticator;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.data.DataStore.DataStoreCloseable;
import de.ultical.backend.exception.AuthorizationException;
import de.ultical.backend.model.Event;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.TournamentFormat;
import de.ultical.backend.model.User;
import io.dropwizard.auth.Auth;

@Path("/format")
public class TournamentFormatResource {

    private static final String LOGIN_MESSAGE = "please login first";
    private static final String UNAUTHORIZED_WARNING = "unauthorized access";
    private static final String DB_ACCESS_FAILED = "database access failed";
    private final static Logger LOGGER = LoggerFactory.getLogger(TournamentFormatResource.class);
    @Inject
    DataStore dataStore;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TournamentFormat> getAll() {
        this.checkDataStore();
        List<TournamentFormat> result = this.dataStore.getAll(TournamentFormat.class);
        return result;
    }

    @GET
    @Path("/own")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TournamentFormat> getByOwner(@Auth @NotNull User currentUser) {
        this.checkDataStore();
        List<TournamentFormat> result = this.dataStore.getFormatByOwner(currentUser.getId());
        return result;
    }

    private void checkDataStore() {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency injection failed!", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TournamentFormat addNewFormat(TournamentFormat tf, @Auth @NotNull User currentUser)  {
        this.checkDataStore();
        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            Authenticator.assureOverallAdmin(currentUser);
            TournamentFormat result = this.dataStore.addNew(tf);
            return result;
        } catch (PersistenceException pe) {
            LOGGER.error(DB_ACCESS_FAILED, pe);
            throw new WebApplicationException(DB_ACCESS_FAILED, pe, Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } catch (AuthorizationException ae) {
            LOGGER.warn(UNAUTHORIZED_WARNING, ae);
            throw new WebApplicationException(LOGIN_MESSAGE, ae, Status.UNAUTHORIZED);
        }
    }

    @PUT
    @Path("/{formatId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateFormat(@PathParam("formatId") Integer formatId, TournamentFormat updatedFormat,
            @Auth @NotNull User currentUser)  {
        this.checkDataStore();
        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            if (formatId.equals(updatedFormat.getId()) == false) {
                throw new WebApplicationException("Request URL and payload do not match!", Status.NOT_ACCEPTABLE);
            }
            final boolean update = this.dataStore.update(updatedFormat);
            if (!update) {
                throw new WebApplicationException("Update failed, eventually someone else updated in the meantime",
                        Status.CONFLICT);
            }
        } catch (PersistenceException pe) {
            LOGGER.error(DB_ACCESS_FAILED, pe);
            throw new WebApplicationException(DB_ACCESS_FAILED, pe, Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } catch (AuthorizationException ae) {
            LOGGER.warn(UNAUTHORIZED_WARNING, ae);
            throw new WebApplicationException(LOGIN_MESSAGE, ae, Status.UNAUTHORIZED);
        }
    }

    @GET
    @Path("/{formatId}")
    @Produces(MediaType.APPLICATION_JSON)
    public TournamentFormat getFormat(@PathParam("formatId") Integer formatId) {
        this.checkDataStore();
        try {
            TournamentFormat result = this.dataStore.get(formatId, TournamentFormat.class);
            if (result == null) {
                throw new WebApplicationException(Status.NOT_FOUND);
            }
            return result;
        } catch (PersistenceException pe) {
            LOGGER.error(DB_ACCESS_FAILED, pe);
            throw new WebApplicationException(DB_ACCESS_FAILED, pe, Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    @GET
    @Path("/event/{eventId}")
    @Produces(MediaType.APPLICATION_JSON)
    public TournamentFormat getFormatByEvent(@PathParam("eventId") Integer eventId)  {
        this.checkDataStore();

        try (DataStoreCloseable c = this.dataStore.getClosable()) {
        	Event event = this.dataStore.get(eventId, de.ultical.backend.model.Event.class);
        	if(event == null) {
        		throw new WebApplicationException(Status.NOT_FOUND);
        	}
        	TournamentEdition edition = event.getTournamentEdition();
        	TournamentFormat format = edition.getTournamentFormat();
        	List<Event> eventList = new ArrayList<Event>();
        	eventList.add(event);
        	edition.setEvents(eventList);
        	List<TournamentEdition> editionList = new ArrayList<TournamentEdition>();
        	editionList.add(edition);
        	format.setEditions(editionList);
            
            return format;
        } catch (PersistenceException pe) {
            LOGGER.error(DB_ACCESS_FAILED, pe);
            throw new WebApplicationException(DB_ACCESS_FAILED, pe, Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    @GET
    @Path("/edition/{editionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public TournamentFormat getFormatByEdition(@PathParam("editionId") Integer editionId)  {
        this.checkDataStore();

        try (DataStoreCloseable c = this.dataStore.getClosable()) {

            TournamentFormat result = this.dataStore.getFormatByEdition(editionId);
            if (result == null) {
                throw new WebApplicationException(Status.NOT_FOUND);
            }
            return result;
        } catch (PersistenceException pe) {
            LOGGER.error(DB_ACCESS_FAILED, pe);
            throw new WebApplicationException(DB_ACCESS_FAILED, pe, Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }
}
