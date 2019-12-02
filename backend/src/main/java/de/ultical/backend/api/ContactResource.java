package de.ultical.backend.api;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.data.DataStore.DataStoreCloseable;
import de.ultical.backend.model.Contact;
import de.ultical.backend.model.ContactType;
import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.List;

@Path("/contact")
public class ContactResource {

    private final static Logger LOG = LoggerFactory.getLogger(ContactResource.class);

    @Inject
    DataStore dataStore;

    @GET
    @Path("/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Contact> getContactsByType(@PathParam("type") ContactType type) {

        try (DataStoreCloseable c = this.dataStore.getClosable()) {
            return this.dataStore.getContactsBy(type);
        } catch (PersistenceException pe) {
            LOG.error("accessing database failed", pe);
            throw new WebApplicationException("Accessing database failed - ContactResource - " + pe.getMessage(),
                    Status.INTERNAL_SERVER_ERROR);
        }
    }

}
