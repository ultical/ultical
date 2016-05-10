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
