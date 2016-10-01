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

import de.ultical.backend.jobs.DfvDataSync;

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
            defaultScheduler.scheduleJob(JobBuilder.newJob(DfvDataSync.class).build(),
                    TriggerBuilder.newTrigger().startNow().build());
            result = true;
        } catch (SchedulerException e) {
            LOGGER.error(String.format("executing job %s failed", DfvDataSync.class.getName()), e);

        }
        return result;
    }

}
