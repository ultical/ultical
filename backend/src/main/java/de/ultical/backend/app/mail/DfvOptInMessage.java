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
            this.setSubject("Änderung der Mailadresse");

            this.addParagraph("du bist beim DFV mit dieser Mailadresse gemeldet, Bei " + values.get("pageName")
                    + " hast du aber " + values.get("registeredEmail")
                    + " als Mailadresse angegeben. Um auf Nummer sicher zu gehen, bestätige bitte die neue Adresse mit dem unten angegebenen Link. Beachte, dass sich die beim DFV hinterlegte Adresse dadurch nicht ändert.");
            this.addParagraph(
                    "Hier klicken (oder den Link in die Adresszeile des Browsers kopieren):\n" + values.get("link"));
        }
    }
}
