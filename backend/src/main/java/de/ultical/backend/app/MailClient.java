/**
 * Copyright (C) 2015-2016 ultical contributors
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 * * Apache License, version 2.0
 * * Apache Software License, version 1.0
 * * Mozilla Public License, versions 1.0, 1.1 and 2.0
 * * Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU Affero General Public License
 * version 3 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the  GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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

    @Inject
    UltiCalConfig config;

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

                if (this.config.getDebugMode().isEnabled() && !this.config.getDebugMode().getMailCatcher().isEmpty()) {
                    String detouredRecipient = recipient.replace("<", "-").replace(">", "-") + " <"
                            + this.config.getDebugMode().getMailCatcher() + ">";
                    message.setRecipient(RecipientType.TO, new InternetAddress(detouredRecipient));
                } else {
                    message.setRecipient(RecipientType.TO, new InternetAddress(recipient));
                }

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
