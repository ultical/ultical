package de.ultical.backend.app;

import java.util.ArrayList;
import java.util.List;
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

import de.ultical.backend.app.MailClient.UlticalMessage.Recipient;
import de.ultical.backend.app.MailClient.UlticalMessage.UlticalRecipientType;
import lombok.Data;

public class MailClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(MailClient.class);

    @Inject
    Session mailSession;

    @Inject
    UltiCalConfig config;

    private String footer = System.lineSeparator() + System.lineSeparator() + "-----" + System.lineSeparator()
            + "www.dfv-turniere.de" + System.lineSeparator() + "Der Turnierkalender des DFV";

    public static interface UlticalMessage {
        public enum UlticalRecipientType {
            TO, CC, BCC, REPLY_TO;
        }

        @Data
        public class Recipient {
            private String email;
            private String name;

            public Recipient(String email) {
                this.setEmail(email);
            }

            public Recipient(String email, String name) {
                this.setEmail(email);
                this.setName(name);
            }

            public String getNameAddress() {
                if (this.name != null && !this.name.isEmpty()) {
                    return this.name + " <" + this.email + ">";
                } else {
                    return this.email;
                }
            }
        }

        Set<Recipient> getRecipients(UlticalRecipientType recipientType);

        String getRenderedMessage();

        String getSubject();

        String getSenderName();
    }

    public void sendMail(UlticalMessage m) {
        Objects.requireNonNull(m, "You must not pass a null-value!");

        try {
            // Transport trans = this.mailSession.getTransport();
            MimeMessage message = new MimeMessage(this.mailSession);

            // TO
            for (Recipient recipient : m.getRecipients(UlticalRecipientType.TO)) {
                if (this.config.getDebugMode().isEnabled() && !this.config.getDebugMode().getMailCatcher().isEmpty()) {
                    String detouredRecipient = recipient.getNameAddress().replace("<", "-").replace(">", "-") + " <"
                            + this.config.getDebugMode().getMailCatcher() + ">";
                    message.setRecipient(RecipientType.TO, new InternetAddress(detouredRecipient));
                } else {
                    message.addRecipient(RecipientType.TO, new InternetAddress(recipient.getNameAddress()));
                }
            }

            // CC
            if (m.getRecipients(UlticalRecipientType.CC) != null) {
                for (Recipient cc : m.getRecipients(UlticalRecipientType.CC)) {
                    if (this.config.getDebugMode().isEnabled()
                            && !this.config.getDebugMode().getMailCatcher().isEmpty()) {
                        String detouredRecipient = cc.getNameAddress().replace("<", "-").replace(">", "-") + " <"
                                + this.config.getDebugMode().getMailCatcher() + ">";
                        message.setRecipient(RecipientType.CC, new InternetAddress(detouredRecipient));
                    } else {
                        message.addRecipient(RecipientType.CC, new InternetAddress(cc.getNameAddress()));
                    }
                }
            }

            // BCC
            if (m.getRecipients(UlticalRecipientType.BCC) != null) {
                for (Recipient bcc : m.getRecipients(UlticalRecipientType.BCC)) {
                    if (!(this.config.getDebugMode().isEnabled()
                            && !this.config.getDebugMode().getMailCatcher().isEmpty())) {
                        message.addRecipients(RecipientType.BCC, bcc.getEmail());
                    }
                }
            }

            // REPLY TO
            if (m.getRecipients(UlticalRecipientType.REPLY_TO) != null
                    && !m.getRecipients(UlticalRecipientType.REPLY_TO).isEmpty()) {
                List<InternetAddress> replyTos = new ArrayList<InternetAddress>();

                for (Recipient replyTo : m.getRecipients(UlticalRecipientType.REPLY_TO)) {
                    replyTos.add(new InternetAddress(replyTo.getNameAddress()));
                }
                message.setReplyTo(replyTos.toArray(new InternetAddress[replyTos.size()]));
            }

            // FROM
            message.setFrom(m.getSenderName() + " <"
                    + this.mailSession.getProperty(SessionFactory.EMAIL_FROM_PROPERTY_KEY) + ">");
            message.setSender(
                    new InternetAddress(this.mailSession.getProperty(SessionFactory.EMAIL_FROM_PROPERTY_KEY)));

            // SUBJECT
            message.setSubject(m.getSubject());

            // BODY
            message.setText(m.getRenderedMessage() + this.footer, "UTF-8");

            // SEND
            Transport.send(message);

        } catch (NoSuchProviderException npe) {
            LOGGER.error("Failed to open transport", npe);
        } catch (MessagingException me) {
            LOGGER.error("Failed to build message", me);
        }
    }
}
