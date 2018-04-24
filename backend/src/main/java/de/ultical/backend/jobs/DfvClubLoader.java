package de.ultical.backend.jobs;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Club;

public class DfvClubLoader {

    @Inject
    private Client client;

    @Inject
    private UltiCalConfig config;

    @Inject
    private DataStore dataStore;

    public boolean getClubs() throws Exception {

        if (!this.config.getJobsConf().isDfvMvSyncEnabled()) {
            return false;
        }

        try (AutoCloseable c = this.dataStore.getClosable()) {

            WebTarget target = this.client.target(this.config.getDfvApi().getUrl()).path("vereine")
                    .queryParam("token", this.config.getDfvApi().getToken())
                    .queryParam("secret", this.config.getDfvApi().getSecret());

            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);

            List<Club> response = invocationBuilder.get(new GenericType<List<Club>>() {
            });

            this.dataStore.refreshClubs(response);

            return true;
        }
    }

}
