package de.ultical.backend.api;

import de.ultical.backend.app.Authenticator;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.data.mapper.RosterMapper;
import de.ultical.backend.data.mapper.SeasonMapper;
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
import java.util.List;

@Path("/edition")
public class TournamentEditionResource {

    private static final String DB_ACCESS_FAILURE = "Accessing database failed";
    private final static Logger LOG = LoggerFactory.getLogger(EventsResource.class);
    private static final String LOGIN_MESSAGE = "please login first";
    private static final String UNAUTHORIZED_WARNING = "unauthorized access";
    private static final String DB_ACCESS_FAILED = "database access failed";
    private final static Logger LOGGER = LoggerFactory.getLogger(TournamentEditionResource.class);
    @Inject
    DataStore dataStore;

    @GET
    @Path("/format/{formatId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TournamentEdition> getByFormat(@PathParam("formatId") Integer formatId, @Auth @NotNull User currentUser) {
        this.checkDataStore();

        List<TournamentEdition> result = this.dataStore.getEditionListingByFormat(formatId);
        return result;
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TournamentEdition addNewEdition(TournamentEdition edition, @Auth @NotNull User currentUser) {
        this.checkDataStore();

        try (DataStore.DataStoreCloseable c = this.dataStore.getClosable()) {

            TournamentFormat format = dataStore.get(edition.getTournamentFormat().getId(), TournamentFormat.class);
            Authenticator.assureFormatAdmin(format, currentUser);
            this.assureContextMatchesAssociation(edition, format);

            edition.setSeason(dataStore.getOrCreateSeason(edition.getSeason()));

            if (edition.getOrganizer().getId() == -1) {
                edition.setOrganizer(dataStore.addNew(edition.getOrganizer()));
            }

            edition = this.dataStore.addNew(edition);

            edition.setVersion(1);

            return edition;
        } catch (PersistenceException pe) {
            LOG.error(DB_ACCESS_FAILURE, pe);
            throw new WebApplicationException(DB_ACCESS_FAILURE, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/{editionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TournamentEdition updateEdition(@PathParam("editionId") Integer id, TournamentEdition edition, @Auth @NotNull User currentUser) {
        this.checkDataStore();
        if (!id.equals(edition.getId())) {
            throw new WebApplicationException("Request URL and payload do not match!", Status.NOT_ACCEPTABLE);
        }

        try (DataStore.DataStoreCloseable c = this.dataStore.getClosable()) {
            TournamentFormat format = dataStore.get(edition.getTournamentFormat().getId(), TournamentFormat.class);
            Authenticator.assureFormatAdmin(format, currentUser);
            this.assureContextMatchesAssociation(edition, format);

            if (edition.getOrganizer().getId() == -1) {
                edition.getOrganizer().setType(ContactType.TOURNAMENT_EDITION);
                edition.setOrganizer(dataStore.addNew(edition.getOrganizer()));
            }

            boolean updated = this.dataStore.update(edition);

            if (!updated) {
                throw new WebApplicationException(
                        "Update failed, eventually someone else update the resource before you", Status.CONFLICT);
            }

            return this.dataStore.get(edition.getId(), TournamentEdition.class);

        } catch (PersistenceException pe) {
            LOG.error(DB_ACCESS_FAILURE, pe);
            throw new WebApplicationException(DB_ACCESS_FAILURE, Status.INTERNAL_SERVER_ERROR);
        }
    }

    private void checkDataStore() {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency injection failed!", Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Ensures the edition's context is selectable for the format's association.
     * A context with no association is universal and always allowed; otherwise
     * the context's association must equal the format's association.
     */
    private void assureContextMatchesAssociation(TournamentEdition edition, TournamentFormat format) {
        if (edition.getContext() == null) {
            return;
        }
        Context context = this.dataStore.get(edition.getContext().getId(), Context.class);
        if (context == null) {
            throw new WebApplicationException("The selected context does not exist", Status.NOT_ACCEPTABLE);
        }
        if (context.getAssociation() == null) {
            return;
        }
        if (format.getAssociation() == null
                || format.getAssociation().getId() != context.getAssociation().getId()) {
            throw new WebApplicationException(
                    "The selected context does not belong to the tournament format's association",
                    Status.NOT_ACCEPTABLE);
        }
    }
}
