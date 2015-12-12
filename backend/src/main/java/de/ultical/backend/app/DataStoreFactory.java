package de.ultical.backend.app;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;

import de.ultical.backend.data.DataStore;

@Singleton
public class DataStoreFactory implements Factory<DataStore> {
	private DataStore internalDStore = null;
	private final ServiceLocator serviceLocator;

	@Inject
	public DataStoreFactory(final ServiceLocator locator) {
		this.serviceLocator = locator;
	}

	@Override
	public DataStore provide() {
		/*
		 * TODO: This factory could be changed once the datastore does completey
		 * rely on the database instead of private collections. Then we could
		 * discard the singleton instance of the dataStore.
		 */
		if (this.internalDStore == null) {
			this.internalDStore = new DataStore();
		}
		/*
		 * as the sqlsession injected into dataSTore is request-scoped, we have
		 * to do this step for each dataStore request. Mind multi-threading!
		 */
		this.serviceLocator.inject(internalDStore);
		return this.internalDStore;
	}

	@Override
	public void dispose(DataStore instance) {

	}
}