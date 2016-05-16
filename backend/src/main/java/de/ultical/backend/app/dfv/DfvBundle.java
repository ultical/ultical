package de.ultical.backend.app.dfv;

import com.codahale.metrics.SharedMetricRegistries;

import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.JobManager;
import de.ultical.backend.app.UltiCalConfig;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class DfvBundle implements ConfiguredBundle<UltiCalConfig> {

    protected String scanURL = "de.ultical.backend.jobs";

    @Override
    public void run(UltiCalConfig configuration, Environment environment) throws Exception {
        if (configuration.isUseDFVRestriction()) {
            // we only do anything, if the Dfv-parts are requested in the config
            // jobBundle run-method
            JobManager jobManager = new JobManager(this.scanURL);
            environment.lifecycle().manage(jobManager);

            // register RegistrationHandler for dfv with higher rank...
            environment.jersey().register(DfvBinder.class);
        }

    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // TODO should be added only if necessary. Still looking for an elegant
        // way to achieve this...
        // bootstrap.addBundle(new JobsBundle("de.ultical.backend.jobs"));
        SharedMetricRegistries.add(Job.DROPWIZARD_JOBS_KEY, bootstrap.getMetricRegistry());
    }

}
