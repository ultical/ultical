/**
 * Copyright (C) 2015-2016 ultical contributors
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 * * Apache License, version 2.0
 * * Apache Software License, version 1.0
 * * Mozilla Public License, versions 1.0, 1.1 and 2.0
 * * Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU Affero General Public License
 * version 3 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the  GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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

        if (!this.config.getJobs().isDfvMvSyncEnabled()) {
            return false;
        }

        try (AutoCloseable c = this.dataStore.getClosable()) {

            WebTarget target = this.client.target(this.config.getDfvApi().getUrl()).path("profile")
                    .queryParam("token", this.config.getDfvApi().getToken())
                    .queryParam("secret", this.config.getDfvApi().getSecret());

            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);

            List<DfvMvName> response = invocationBuilder.get(new GenericType<List<DfvMvName>>() {
            });

            for (DfvMvName dfvMvName : response) {
                dfvMvName.setFirstName(dfvMvName.getFirstName().trim());
                dfvMvName.setLastName(dfvMvName.getLastName().trim());
            }

            this.dataStore.refreshDfvNames(response);

            return true;
        }
    }

}
