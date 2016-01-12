package de.ultical.backend.app.mail;

import java.util.Map;

public class ConfirmEmailMessage extends DefaultMessage {

    public ConfirmEmailMessage() {
        super();
    }

    public ConfirmEmailMessage(String language) {
        super(language);
    }

    @Override
    public void init(Map<String, String> values) {

        switch (this.getLanguage().toLowerCase()) {
        case "de":
        default:
            this.setSubject("Registrierung bei " + values.get("pageName") + " - bitte bestätigen");

            this.addParagraph("willkommen bei " + values.get("pageName")
                    + ". Um deinen Account zu aktivieren, bestätige bitte deine E-Mail-Adresse. Klicke dafür auf den folgenden Link. Danach kannst du die Seite komplett nutzen.");
            this.addParagraph(
                    "Hier klicken (oder den Link in die Adresszeile des Browsers kopieren):\n" + values.get("link"));

            if (values.get("needsDfvOptIn").equals("1")) {
                this.addParagraph("Da du beim DFV mit einer anderen E-Mail-Adresse gemeldet bist, als du bei "
                        + values.get("pageName")
                        + " angegeben hast, erhälst du auch an diese Adresse eine Bestätigungsemail.");
            }
        }
    }
}
