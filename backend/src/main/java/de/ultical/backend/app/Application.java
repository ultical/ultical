package de.ultical.backend.app;

import java.time.LocalDate;
import java.util.EnumSet;

import javax.mail.Session;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.ws.rs.client.Client;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import de.spinscale.dropwizard.jobs.JobsBundle;
import de.ultical.backend.api.AuthResource;
import de.ultical.backend.api.ClubResource;
import de.ultical.backend.api.ContextResource;
import de.ultical.backend.api.DfvMvNameResource;
import de.ultical.backend.api.DivisionResource;
import de.ultical.backend.api.EventsResource;
import de.ultical.backend.api.MailResource;
import de.ultical.backend.api.RegisterResource;
import de.ultical.backend.api.RosterResource;
import de.ultical.backend.api.SeasonResource;
import de.ultical.backend.api.SitemapResource;
import de.ultical.backend.api.TeamResource;
import de.ultical.backend.api.TournamentFormatResource;
import de.ultical.backend.api.TournamentResource;
import de.ultical.backend.api.UserResource;
import de.ultical.backend.app.logging.UlticalLoggingFilter;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.data.LocalDateMixIn;
import de.ultical.backend.data.mapper.UserMapper;
import de.ultical.backend.model.User;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;

public class Application extends io.dropwizard.Application<UltiCalConfig> {

    public static void main(String[] args) throws Exception {
        Application ultiCal = new Application();
        ultiCal.run(args);
    }

    @Override
    public void initialize(Bootstrap<UltiCalConfig> bootstrap) {
        super.initialize(bootstrap);

        ObjectMapper objectMapper = bootstrap.getObjectMapper();
        objectMapper.addMixIn(LocalDate.class, LocalDateMixIn.class);

        // add Jobs bundle to provide schedules tasks
        bootstrap.addBundle(new JobsBundle("de.ultical.backend.jobs"));
    }

    @Override
    public void run(UltiCalConfig config, Environment env) throws Exception {

        ManagedDataSource mds = config.getDatabase().build(env.metrics(), "UltiCal DataSource");
        env.lifecycle().manage(mds);
        /*
         * We create a MyBatisManager and register it with the
         * dropwizard-lifecylce system. This ensures that MYBatis is started,
         * when the dropwizard environment starts and stopped accordingly.
         */
        final MyBatisManager mbm = new MyBatisManager(mds);
        env.lifecycle().manage(mbm);
        env.jersey().register(new AbstractBinder() {

            @Override
            protected void configure() {
                /*
                 * we use the MyBatisManager as a factory to provide access to a
                 * SqlSession.
                 */
                this.bindFactory(mbm).to(SqlSession.class);
                this.bindFactory(DataStoreFactory.class).to(DataStore.class);

                // Create factory to inject Client
                this.bindFactory(new Factory<Client>() {

                    private Client clientInstance;

                    @Override
                    public void dispose(Client instance) {
                        if (instance != null) {
                            instance.close();
                        }
                    }

                    @Override
                    public Client provide() {

                        if (this.clientInstance == null) {
                            JerseyClientConfiguration conf = new JerseyClientConfiguration();
                            conf.setTimeout(Duration.milliseconds(7000));
                            conf.setConnectionTimeout(Duration.milliseconds(7000));

                            this.clientInstance = new JerseyClientBuilder(env).using(conf).using(env).build("dfvApi");
                        }
                        return this.clientInstance;
                    }

                }).to(Client.class);

                this.bindFactory(new Factory<UltiCalConfig>() {

                    @Override
                    public UltiCalConfig provide() {
                        return config;
                    }

                    @Override
                    public void dispose(UltiCalConfig instance) {
                    }

                }).to(UltiCalConfig.class);
                this.bindAsContract(MailClient.class);
                this.bindFactory(SessionFactory.class).to(Session.class);

            }
        });

        // add healthcheck
        env.healthChecks().register("Database healthcheck", new DatabaseHealthCheck(mds));
        env.healthChecks().register("E-Mail health check", new MailHealthCheck());

        env.jersey().register(EventsResource.class);
        env.jersey().register(TournamentResource.class);
        env.jersey().register(SeasonResource.class);
        env.jersey().register(TournamentFormatResource.class);
        env.jersey().register(TeamResource.class);
        env.jersey().register(RegisterResource.class);
        env.jersey().register(AuthResource.class);
        env.jersey().register(DivisionResource.class);
        env.jersey().register(UserResource.class);
        env.jersey().register(RosterResource.class);
        env.jersey().register(DfvMvNameResource.class);
        env.jersey().register(MailResource.class);
        env.jersey().register(ClubResource.class);
        env.jersey().register(ContextResource.class);
        env.jersey().register(SitemapResource.class);

        env.jersey().register(UlticalLoggingFilter.class);

        /*
         * Authentication stuff. Basically the authenticator looks up the
         * provided user-name in the database and compares the password stored
         * in the db with the provided password. If these two match, it returns
         * the corresponding user object. In order to reduce database access the
         * results are cached by a CachingAuthenticator. The
         * AuthValueFactoryProvider could be used to inject the current user
         * into resource methods that need access to the current user. TODO: An
         * authorizer is still missing that assigns each user a role. However,
         * except for a few users which will be always admins the admin role
         * depends on the tournament-format or tournament-edition that is to be
         * changed.
         */
        Authenticator<BasicCredentials, User> authenticator = new Authenticator<BasicCredentials, User>() {

            @Override
            public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
                final String providedUserName = credentials.getUsername();
                final String providedPassword = credentials.getPassword();
                SqlSession sqlSession = mbm.provide();
                User user = null;
                try {
                    UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
                    user = userMapper.getByEmail(providedUserName);
                } catch (PersistenceException pe) {
                    throw new AuthenticationException("Accessing the database failed", pe);
                } finally {
                    sqlSession.close();
                }
                Optional<User> result = Optional.absent();
                if (user != null && user.getPassword().equals(providedPassword)) {
                    result = Optional.of(user);
                }
                return result;
            }
        };

        CachingAuthenticator<BasicCredentials, User> cachingAuthenticator = new CachingAuthenticator<BasicCredentials, User>(
                env.metrics(), authenticator, config.getAuthenticationCache());
        env.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
                .setAuthenticator(cachingAuthenticator).buildAuthFilter()));
        env.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));

        env.jersey().register(ServiceLocatorFeature.class);

        if (config.getDebugMode().isEnabled()) {
            env.jersey().property("jersey.config.server.tracing.type", "ALL");
        }

        if (config.isCorsFilterEnabled()) {
            this.addCorsFilter(env);
        }

	/*
	 * add overall admins
	 */
	if (config.getOverallAdmins() != null) {
	    config.getOverallAdmins().stream().forEach(de.ultical.backend.app.Authenticator::addAdmin);
	}
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
        corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM,
                "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        corsFilter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
    }

}
