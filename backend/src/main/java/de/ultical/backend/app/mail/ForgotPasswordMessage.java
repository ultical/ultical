package de.ultical.backend.app.mail;

import java.util.Map;

public class ForgotPasswordMessage extends SystemMessage {

    public ForgotPasswordMessage() {
        super();
    }

    public ForgotPasswordMessage(Map<String, String> values) {
        super();
        this.init(values);
    }

    public ForgotPasswordMessage(String language) {
        super(language);
    }

    public ForgotPasswordMessage(String language, Map<String, String> values) {
        super(language);
        this.init(values);
    }

    @Override
    public void init(Map<String, String> values) {

        switch (this.getLanguage().toLowerCase()) {
        case "de":
        default:
            this.setSubject("Passwort zurücksetzen");

            this.addParagraph(
                    "du hast diese Mail angefordert, weil du dein Passwort vergessen hast. Bitte benutze den folgenden Link, um ein neues Passwort zu setzen.");
            this.addParagraph(
                    "Hier klicken (oder den Link in die Adresszeile des Browsers kopieren):\n" + values.get("link"));
        }
    }
}
