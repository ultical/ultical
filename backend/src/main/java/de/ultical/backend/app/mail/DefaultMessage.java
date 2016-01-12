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
public class DefaultMessage implements UlticalMessage {

    private final String DEFAULT_LANGUAGE = "de";

    private String language;
    private String subject;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Map<String, String> recipients;

    private List<String> paragraphs;

    private String greetings;

    private String goodbye;
    private String goodbyeName;

    private String senderName;

    private String footer;

    public DefaultMessage() {
        this("de");
    }

    public DefaultMessage(String language) {
        this.setLanguage(language);

        this.recipients = new HashMap<String, String>();
        this.paragraphs = new ArrayList<String>();

        switch (language.toLowerCase()) {
        case "de":
        default:
            this.setSenderName("DFV-Turniere");
            this.setGreetings("Hallo");
            this.setGoodbye("Viele Grüße,");
            this.setGoodbyeName("DFV-Turniere");
            this.setFooter(System.lineSeparator() + "-----" + System.lineSeparator() + "www.dfv-turniere.de"
                    + System.lineSeparator() + "Der Turnierkalender des DFV");
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
        this.recipients.put(this.createFullRecipient(email, fullName), firstName);
    }

    private String createFullRecipient(String email, String name) {
        StringBuilder recipientSb = new StringBuilder();

        if (name != null && !name.isEmpty()) {
            recipientSb.append(name).append(" <");
        }
        recipientSb.append(email);
        if (name != null && !name.isEmpty()) {
            recipientSb.append(">");
        }
        return recipientSb.toString();
    }

    @Override
    public Set<String> getRecipients() {
        return this.recipients.keySet();
    }

    public void addParagraph(String paragraph) {
        this.paragraphs.add(paragraph);
    }

    public boolean readyToSend() {
        return !(this.getRecipients().isEmpty() || this.getParagraphs().isEmpty() || this.getSubject().isEmpty());
    }

    @Override
    public String getRenderedMessage(String email) {
        StringBuilder sb = new StringBuilder();
        String nl = System.lineSeparator();

        sb.append(this.getGreetings());

        if (this.recipients.get(email) != null && !this.recipients.get(email).isEmpty()) {
            sb.append(" ").append(this.recipients.get(email));
        }
        sb.append(",").append(nl).append(nl);

        for (String paragraph : this.getParagraphs()) {
            sb.append(paragraph).append(nl).append(nl);
        }

        sb.append(this.getGoodbye()).append(nl);
        sb.append(this.getGoodbyeName()).append(nl);

        sb.append(this.getFooter());

        return sb.toString();
    }

}
