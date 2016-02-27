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

import org.glassfish.hk2.api.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.Every;
import de.ultical.backend.app.ServiceLocatorProvider;

/**
 * Job to get profile-overview from dfv-mv.de's API every night at 3 a.m. and on
 * application startup
 *
 * @author bas
 *
 */
// @OnApplicationStart bb: had to remove this, as the configuration is not
// available, when the scheduler starts!
// @On("0 0 0/1 * * ?")
@Every("1h")
public class DfvDataSync extends Job {

    private final static Logger LOGGER = LoggerFactory.getLogger(DfvDataSync.class);

    @Override
    public void doJob() {

        LOGGER.info("Starting to fetch and store from dfv-mv...");
        ServiceLocator sl = ServiceLocatorProvider.INSTANCE.getServiceLocator();

        if (sl != null) {

            // get the associations, because the clubs refer to it
            DfvAssociationLoader dal = sl.createAndInitialize(DfvAssociationLoader.class);

            try {
                dal.getAssociations();
            } catch (Exception pe) {
                LOGGER.error("Updating DFV associations failed!", pe);
            }

            // get the clubs, because the profiles refer to it
            DfvClubLoader dcl = sl.createAndInitialize(DfvClubLoader.class);

            try {
                dcl.getClubs();
            } catch (Exception pe) {
                LOGGER.error("Updating DFV clubs failed!", pe);
            }

            DfvProfileLoader diw = sl.createAndInitialize(DfvProfileLoader.class);

            try {
                diw.getDfvMvNames();
            } catch (Exception pe) {
                LOGGER.error("Updating DFV profiles failed!", pe);
            }

        }

        LOGGER.info("... Job finished!");
    }

}
