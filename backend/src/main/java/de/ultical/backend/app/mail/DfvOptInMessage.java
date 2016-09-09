package de.ultical.backend.app.mail;

import java.util.Map;

public class DfvOptInMessage extends SystemMessage {

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
            this.setSubject("Änderung der Mailadresse");

            this.addParagraph("du bist beim DFV mit dieser Mailadresse gemeldet, Bei " + values.get("pageName")
                    + " hast du aber " + values.get("registeredEmail")
                    + " als Mailadresse angegeben. Um auf Nummer sicher zu gehen, bestätige bitte die neue Adresse mit dem unten angegebenen Link. Beachte, dass sich die beim DFV hinterlegte Adresse dadurch nicht ändert.");
            this.addParagraph(
                    "Hier klicken (oder den Link in die Adresszeile des Browsers kopieren):\n" + values.get("link"));
        }
    }
}
