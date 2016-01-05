package de.ultical.backend.api;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;

/**
 * Temporary resource to init server
 *
 * @author bas
 *
 */
@Path("/init")
public class TempInitResource {

    @Inject
    private Client client;

    @Inject
    private UltiCalConfig config;

    @Inject
    private DataStore dataStore;

    @GET
    public boolean initRequest() {

        this.dataStore.setAutoCloseSession(false);

        WebTarget target = this.client.target(this.config.getDfvApi().getUrl()).path("profile")
                .queryParam("token", this.config.getDfvApi().getToken())
                .queryParam("secret", this.config.getDfvApi().getSecret());

        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);

        List<DfvMvName> response = invocationBuilder.get(new GenericType<List<DfvMvName>>() {
        });

        this.dataStore.refreshDfvNames(response);
        this.dataStore.closeSession();

        return true;
    }

}
