package de.ultical.backend.app;

import java.time.LocalDate;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.apache.ibatis.session.SqlSession;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ultical.backend.api.EventsResource;
import de.ultical.backend.api.TournamentResource;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.data.LocalDateMixIn;
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

		ObjectMapper objectMapper = bootstrap.getObjectMapper();
		objectMapper.addMixIn(LocalDate.class, LocalDateMixIn.class);
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

		this.addCorsFilter(env);

		env.jersey().register(EventsResource.class);
		env.jersey().register(TournamentResource.class);
	}

	/*
	 * Add CORS filter to allow frontend to send requests to server
	 */
	private void addCorsFilter(Environment env) {
		FilterRegistration.Dynamic corsFilter = env.servlets().addFilter("CORSFilter", CrossOriginFilter.class);

		// Add URL mapping
		corsFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
		corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
		corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		corsFilter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
		corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
		corsFilter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
	}

}
