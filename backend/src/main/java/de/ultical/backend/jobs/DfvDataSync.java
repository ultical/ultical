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
        ServiceLocator sl = ServiceLocatorProvider.getInstance().getServiceLocator();

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
