package de.ultical.backend.app;

import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(MailClient.class);

    @Inject
    Session mailSession;

    public static interface UlticalMessage {
        Set<String> getRecipients();

        String getRenderedMessage(String recipient);

        String getSubject();

        String getSenderName();
    }

    public void sendMail(UlticalMessage m) {
        Objects.requireNonNull(m, "You must not pass a null-value!");

        try {
            // Transport trans = this.mailSession.getTransport();
            for (String recipient : m.getRecipients()) {
                MimeMessage message = new MimeMessage(this.mailSession);
                message.setSubject(m.getSubject());
                // message.setContent(m.getRenderedMessage(recipient),
                // MediaType.TEXT_PLAIN);
                message.setText(m.getRenderedMessage(recipient), "UTF-8");
                message.setRecipient(RecipientType.TO, new InternetAddress(recipient));
                message.setFrom(m.getSenderName() + " <"
                        + this.mailSession.getProperty(SessionFactory.EMAIL_FROM_PROPERTY_KEY) + ">");
                message.setSender(
                        new InternetAddress(this.mailSession.getProperty(SessionFactory.EMAIL_FROM_PROPERTY_KEY)));
                Transport.send(message);
            }

        } catch (NoSuchProviderException npe) {
            LOGGER.error("Failed to open transport", npe);
        } catch (MessagingException me) {
            LOGGER.error("Failed to build message", me);
        }
    }
}
