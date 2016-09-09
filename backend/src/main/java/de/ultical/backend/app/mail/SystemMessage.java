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
