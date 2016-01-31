package de.ultical.backend.jobs;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;

public class DfvProfileLoader {

    @Inject
    private Client client;

    @Inject
    private UltiCalConfig config;

    @Inject
    private DataStore dataStore;

    public boolean getDfvMvNames() throws Exception {

        if (!this.config.getDfvApi().isDfvMvSyncEnabled()) {
            return false;
        }

        try (AutoCloseable c = this.dataStore.getClosable()) {

            WebTarget target = this.client.target(this.config.getDfvApi().getUrl()).path("profile")
                    .queryParam("token", this.config.getDfvApi().getToken())
                    .queryParam("secret", this.config.getDfvApi().getSecret());

            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);

            List<DfvMvName> response = invocationBuilder.get(new GenericType<List<DfvMvName>>() {
            });

            this.dataStore.refreshDfvNames(response);

            return true;
        }
    }

}
