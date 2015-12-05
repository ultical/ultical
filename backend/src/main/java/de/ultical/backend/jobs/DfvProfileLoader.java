package de.ultical.backend.jobs;

import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.On;
import de.spinscale.dropwizard.jobs.annotations.OnApplicationStart;

/**
 * Job to get profile-overview from dfv-mv.de's API every night at 3 a.m. and on
 * application startup
 *
 * @author bas
 *
 */
@OnApplicationStart
@On("0 0 3 * * ?")
public class DfvProfileLoader extends Job {

	@Override
	public void doJob() {

		// logic run once on application start
	}

}
