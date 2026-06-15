package de.ultical.backend.api;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.data.DataStore.DataStoreCloseable;
import de.ultical.backend.model.Context;

@Path("/context")
public class ContextResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(ContextResource.class);

    @Inject
    DataStore dataStore;

    @GET
    @Path("/{contextId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Context getContextById(@PathParam("contextId") Integer id) {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency Injection for data store failed!",
                    Status.INTERNAL_SERVER_ERROR);
        }
        Context result = null;
        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            result = this.dataStore.get(id, Context.class);
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
    public List<Context> getAllContexts() {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency Injectino for data store failed!",
                    Status.INTERNAL_SERVER_ERROR);
        }
        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            return this.dataStore.getAll(Context.class);
        } catch (PersistenceException pe) {
            LOGGER.error("Database access failed!", pe);
            throw new WebApplicationException("Accessing the database failed", Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Returns the contexts selectable for the given tournament format: those
     * tied to the format's association plus universal (association-less) ones.
     * If the format has no association, all contexts are returned.
     */
    @GET
    @Path("/format/{formatId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Context> getContextsForFormat(@PathParam("formatId") Integer formatId) {
        if (this.dataStore == null) {
            throw new WebApplicationException("Dependency Injection for data store failed!",
                    Status.INTERNAL_SERVER_ERROR);
        }
        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            // Read only the association id; hydrating the full format would
            // eagerly pull in every edition and its sub-graph (see #perf).
            Integer associationId = this.dataStore.getFormatAssociationId(formatId);
            if (associationId == null) {
                // format has no association (or does not exist): every context
                // is selectable
                return this.dataStore.getAll(Context.class);
            }
            return this.dataStore.getContextsByAssociation(associationId);
        } catch (PersistenceException pe) {
            LOGGER.error("Database access failed!", pe);
            throw new WebApplicationException("Accessing the database failed", Status.INTERNAL_SERVER_ERROR);
        }
    }

}
