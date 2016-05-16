package de.ultical.backend.app;

import javax.mail.Session;
import javax.ws.rs.client.Client;

import org.apache.ibatis.session.SqlSession;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import de.ultical.backend.data.DataStore;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;

/**
 * An <code>AbstractBinder</code> implementation dedicated to the needs of the
 * ultical Application.
 * 
 * @author bb
 *
 */
class UlticalBinder extends AbstractBinder {
    private final MyBatisManager mbm;
    private final Environment env;
    private final UltiCalConfig config;

    UlticalBinder(MyBatisManager mbm, final Environment environment, final UltiCalConfig conf) {
        this.mbm = mbm;
        this.env = environment;
        this.config = conf;
    }

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
        this.bind(UltiCalRegistrationHandler.class).to(RegistrationHandler.class).ranked(1);
    }
}