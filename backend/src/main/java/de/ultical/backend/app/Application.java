package de.ultical.backend.app;

import org.apache.ibatis.session.SqlSession;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import de.ultical.backend.api.EventsResource;
import de.ultical.backend.api.TournamentResource;
import de.ultical.backend.data.DataStore;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class Application extends io.dropwizard.Application<UltiCalConfig> {

	public static void main(String[] args) throws Exception {
		Application ultiCal = new Application();
		ultiCal.run(args);
	}

	@Override
	public void initialize(Bootstrap<UltiCalConfig> bootstrap) {
		// TODO Auto-generated method stub
		super.initialize(bootstrap);
	}

	@Override
	public void run(UltiCalConfig config, Environment env) throws Exception {
		/*
		 * We create a MyBatisManager and register it with the
		 * dropwizard-lifecylce system. This ensures that MYBatis is started,
		 * when the dropwizard environment starts and stopped accordingly.
		 */
		final MyBatisManager mbm = new MyBatisManager();
		env.lifecycle().manage(mbm);
		env.jersey().register(new AbstractBinder() {

			@Override
			protected void configure() {
				/*
				 * we use the MyBatisManager as a factory to provide access to a
				 * SqlSession.
				 */
				this.bindFactory(mbm).to(SqlSession.class);
				this.bindFactory(new Factory<DataStore>() {

					private DataStore internalDStore = null;

					@Override
					public DataStore provide() {
						if (this.internalDStore == null) {
							this.internalDStore = new DataStore();
						}
						return this.internalDStore;
					}

					@Override
					public void dispose(DataStore instance) {
					}
				}).to(DataStore.class);

			}
		});

		env.jersey().register(EventsResource.class);
		env.jersey().register(TournamentResource.class);
	}

}
