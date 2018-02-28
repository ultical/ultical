package de.ultical.backend.jobs;

import org.glassfish.hk2.api.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.On;
import de.ultical.backend.app.ServiceLocatorProvider;

@On("0 0 5 * * ?")
public class TravelCompensationJob extends Job {
	private static final Logger LOGGER = LoggerFactory.getLogger(TravelCompensationJob.class);

	@Override
	public void doJob() {
		LOGGER.info("Job started");
		ServiceLocator locator = ServiceLocatorProvider.getInstance().getServiceLocator();
		if (locator != null) {
			TravelCompensationCalculator tcc = locator.createAndInitialize(TravelCompensationCalculator.class);
			tcc.calculate();
		} else {
			LOGGER.warn("no serviceLocator found. Probably that's not really good news");
		}
		LOGGER.info("Job finished");
	}

}
