package de.ultical.backend.jobs;

import javax.ws.rs.WebApplicationException;

import org.apache.ibatis.exceptions.PersistenceException;
import org.glassfish.hk2.api.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.On;
import de.ultical.backend.api.TempInitResource;
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
public class DfvProfileLoader extends Job {

    private final static Logger LOGGER = LoggerFactory.getLogger(DfvProfileLoader.class);

    @Override
    public void doJob() {
        LOGGER.info("Job started ...");
        ServiceLocator sl = ServiceLocatorProvider.INSTANCE.getServiceLocator();
        if (sl != null) {
            TempInitResource tir = sl.createAndInitialize(TempInitResource.class);
            try {
                tir.initRequest();
            } catch (WebApplicationException | PersistenceException pe) {
                LOGGER.error("Updating DFV profiles failed!", pe);
            }
        }
        LOGGER.info("... Job finished!");
    }

}
