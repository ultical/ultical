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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ultical.backend.app.MailClient.UlticalMessage;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class SystemMessage implements UlticalMessage {

    private final String DEFAULT_LANGUAGE = "de";

    private String language;
    private String subject;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Map<Recipient, String> recipients;

    private List<String> paragraphs;

    private String greetings;

    private String goodbye;
    private String goodbyeName;

    private String senderName;

    public SystemMessage() {
        this("de");
    }

    public SystemMessage(String language) {
        this.setLanguage(language);

        this.recipients = new HashMap<Recipient, String>();
        this.paragraphs = new ArrayList<String>();

        switch (language.toLowerCase()) {
        case "de":
        default:
            this.setSenderName("DFV-Turniere");
            this.setGreetings("Hallo");
            this.setGoodbye("Viele Grüße");
            this.setGoodbyeName("DFV-Turniere");
        }
    }

    public void init(Map<String, String> values) {
    }

    public void addRecipient(String email) {
        this.addRecipient(email, null);
    }

    public void addRecipient(String email, String name) {
        this.addRecipient(email, name, name);
    }

    public void addRecipient(String email, String firstName, String fullName) {
        Recipient recipient = new Recipient(email);
        recipient.setName(fullName);
        this.recipients.put(recipient, firstName);
    }

    @Override
    public Set<Recipient> getRecipients(UlticalRecipientType recipientType) {
        if (recipientType == UlticalRecipientType.TO) {
            return this.recipients.keySet();
        } else {
            return null;
        }
    }

    public void addParagraph(String paragraph) {
        this.paragraphs.add(paragraph);
    }

    @Override
    public String getRenderedMessage() {
        StringBuilder sb = new StringBuilder();
        String nl = System.lineSeparator();

        sb.append(this.getGreetings());

        String recipientName = "";
        if (this.recipients.size() == 1) {
            for (String recipient : this.recipients.values()) {
                recipientName = recipient;
            }
        }
        if (recipientName != null && !recipientName.isEmpty()) {
            sb.append(" ").append(recipientName);
        }
        sb.append(",").append(nl).append(nl);

        for (String paragraph : this.getParagraphs()) {
            sb.append(paragraph).append(nl).append(nl);
        }

        sb.append(this.getGoodbye()).append(nl);
        sb.append(this.getGoodbyeName()).append(nl);

        return sb.toString();
    }
}
