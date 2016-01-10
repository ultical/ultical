package de.ultical.backend.app;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.hk2.api.ServiceLocator;
import org.quartz.JobBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ultical.backend.jobs.DfvProfileLoader;

public class ServiceLocatorFeature implements Feature {
    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceLocatorFeature.class);
    private ServiceLocator serviceLocator;

    @Inject
    public ServiceLocatorFeature(ServiceLocator sl) {
        this.serviceLocator = sl;
    }

    @Override
    public boolean configure(FeatureContext context) {
        ServiceLocatorProvider.INSTANCE.setServiceLocator(this.serviceLocator);
        boolean result = false;
        try {
            final Scheduler defaultScheduler = StdSchedulerFactory.getDefaultScheduler();
            defaultScheduler.scheduleJob(JobBuilder.newJob(DfvProfileLoader.class).build(),
                    TriggerBuilder.newTrigger().startNow().build());
            result = true;
        } catch (SchedulerException e) {
            LOGGER.error(String.format("executing job %s failed", DfvProfileLoader.class.getName()), e);

        }
        return result;
    }

}
