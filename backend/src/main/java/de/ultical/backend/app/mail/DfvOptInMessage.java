package de.ultical.backend.app.mail;

import java.util.Map;

public class DfvOptInMessage extends DefaultMessage {

    public DfvOptInMessage() {
        super();
    }

    public DfvOptInMessage(String language) {
        super(language);
    }

    @Override
    public void init(Map<String, String> values) {

        switch (this.getLanguage().toLowerCase()) {
        case "de":
        default:
            this.setSubject("Änderung der E-Mail-Adresse");

            this.addParagraph("du bist beim DFV mit einer anderen E-Mail-Adresse gemeldet, als du bei "
                    + values.get("pageName")
                    + " angegeben hast. Um auf Nummer sicher zu gehen, bestätige bitte auch diese Mail mit dem unten angegebenen Link.");
            this.addParagraph(
                    "Hier klicken (oder den Link in die Adresszeile des Browsers kopieren):\n" + values.get("link"));
        }
    }
}
