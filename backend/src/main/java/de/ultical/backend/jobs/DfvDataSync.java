package de.ultical.backend.jobs;

import javax.ws.rs.WebApplicationException;

import org.apache.ibatis.exceptions.PersistenceException;
import org.glassfish.hk2.api.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.On;
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
@On("0 0 3 * * ?")
public class DfvDataSync extends Job {

    private final static Logger LOGGER = LoggerFactory.getLogger(DfvDataSync.class);

    @Override
    public void doJob() {

        LOGGER.info("Starting to fetch and store from dfv-mv...");
        ServiceLocator sl = ServiceLocatorProvider.INSTANCE.getServiceLocator();

        if (sl != null) {
            DfvProfileLoader diw = sl.createAndInitialize(DfvProfileLoader.class);

            try {
                diw.getDfvMvNames();
            } catch (WebApplicationException | PersistenceException pe) {
                LOGGER.error("Updating DFV profiles failed!", pe);
            }

            DfvClubLoader dcl = sl.createAndInitialize(DfvClubLoader.class);

            try {
                dcl.getClubs();
            } catch (WebApplicationException | PersistenceException pe) {
                LOGGER.error("Updating DFV clubs failed!", pe);
            }
        }

        LOGGER.info("... Job finished!");
    }

}
