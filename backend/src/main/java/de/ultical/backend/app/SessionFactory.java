package de.ultical.backend.app;

import java.util.Properties;

import javax.inject.Inject;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.glassfish.hk2.api.Factory;

public class SessionFactory implements Factory<Session> {

    public final static String EMAIL_FROM_PROPERTY_KEY = "de.ultical.mail.from";

    @Inject
    UltiCalConfig config;

    @Override
    public Session provide() {
        final Properties props = new Properties();
        props.setProperty("mail.smtp.host", this.config.getMail().getSmtpHost());
        props.setProperty("mail.smtp.port", this.config.getMail().getSmtpPort());
        props.setProperty("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty(EMAIL_FROM_PROPERTY_KEY, this.config.getMail().getSmtpSender());
        Session mailSession = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SessionFactory.this.config.getMail().getSmtpUser(),
                        SessionFactory.this.config.getMail().getSmtpPassword());
            }
        });
        return mailSession;
    }

    @Override
    public void dispose(Session instance) {

    }

}
