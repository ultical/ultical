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
package de.ultical.backend.app.mail;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.ultical.backend.app.MailClient.UlticalMessage;
import lombok.Data;

@Data
public class UserMessage implements UlticalMessage {

    private Map<UlticalRecipientType, Set<Recipient>> recipients;

    private String subject;
    private String body;
    private Recipient author;
    private String authorDescriptionText;

    public UserMessage() {
        this.recipients = new HashMap<UlticalRecipientType, Set<Recipient>>();
        this.recipients.put(UlticalRecipientType.TO, new HashSet<Recipient>());
        this.recipients.put(UlticalRecipientType.CC, new HashSet<Recipient>());
        this.recipients.put(UlticalRecipientType.BCC, new HashSet<Recipient>());
        this.recipients.put(UlticalRecipientType.REPLY_TO, new HashSet<Recipient>());
    }

    @Override
    public Set<Recipient> getRecipients(UlticalRecipientType recipientType) {
        return this.recipients.get(recipientType);
    }

    public void addRecipient(UlticalRecipientType recipientType, Recipient recipient) {
        this.recipients.get(recipientType).add(recipient);
    }

    public void addRecipients(UlticalRecipientType recipientType, Collection<Recipient> recipients) {
        this.recipients.get(recipientType).addAll(recipients);
    }

    @Override
    public String getRenderedMessage() {
        StringBuilder sb = new StringBuilder();
        String nl = System.lineSeparator();

        sb.append(this.getBody());

        sb.append(nl).append(nl);

        // Sender description text
        if (!this.getAuthorDescriptionText().isEmpty()) {
            sb.append("-----").append(nl).append(this.getAuthorDescriptionText());
        }

        return sb.toString();
    }

    @Override
    public String getSenderName() {
        return this.getAuthor().getName() + " über DFV-Turniere";
    }

}
