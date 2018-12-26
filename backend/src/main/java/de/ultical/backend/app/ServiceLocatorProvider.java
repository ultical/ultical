package de.ultical.backend.app;

import org.glassfish.hk2.api.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceLocatorProvider {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLocatorProvider.class);
    private static ServiceLocatorProvider INSTANCE = null;

    private ServiceLocatorProvider() {}

    public static ServiceLocatorProvider getInstance() {
	if (INSTANCE == null) {
	    INSTANCE = new ServiceLocatorProvider();
	}
	return INSTANCE;
    }

    private ServiceLocator serviceLocator = null;

    public void setServiceLocator(final ServiceLocator newSl) {
        if (this.serviceLocator != null) {
            LOGGER.warn("You are replacing the ServiceLocator. Hopefully, you know what you are doing!");
        }
        this.serviceLocator = newSl;
        LOGGER.debug("ServiceLocator replaced");
    }

    public ServiceLocator getServiceLocator() {
        return this.serviceLocator;
    }
}
