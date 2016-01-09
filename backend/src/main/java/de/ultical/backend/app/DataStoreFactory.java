package de.ultical.backend.app;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;

import de.ultical.backend.data.DataStore;

@Singleton
public class DataStoreFactory implements Factory<DataStore> {
    private final ServiceLocator serviceLocator;

    @Inject
    public DataStoreFactory(final ServiceLocator locator) {
        this.serviceLocator = locator;
    }

    @Override
    public DataStore provide() {
        final DataStore result = this.serviceLocator.createAndInitialize(DataStore.class);
        return result;
    }

    @Override
    public void dispose(DataStore instance) {
        if (instance != null) {
            this.serviceLocator.preDestroy(instance);
        }
    }
}