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
package de.ultical.backend.app;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;

import de.ultical.backend.app.mail.ConfirmEmailMessage;
import de.ultical.backend.app.mail.DefaultMessage;
import de.ultical.backend.app.mail.DfvOptInMessage;
import de.ultical.backend.app.mail.ForgotPasswordMessage;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.MailCode;
import de.ultical.backend.model.MailCodeType;
import de.ultical.backend.model.User;

public class EmailCodeService {

    private String websiteUrl;
    private SecureRandom secureRandom;
    private DataStore dataStore;

    public EmailCodeService(DataStore dataStore, String websiteUrl) {
        this.dataStore = dataStore;
        this.websiteUrl = websiteUrl;
        this.secureRandom = new SecureRandom();
    }

    public boolean sendForgotPasswordMessage(MailClient mailClient, User user) {

        MailCode mailCode = this.createMailCode(user, MailCodeType.FORGOT_PASSWORD);

        if (this.dataStore.saveMailCode(mailCode)) {
            HashMap<String, String> values = new HashMap<String, String>();
            values.put("link", this.createForgotPasswordLink(mailCode));
            DefaultMessage message = new ForgotPasswordMessage(values);
            message.addRecipient(user.getEmail(), user.getDfvPlayer().getFirstName(), user.getFullName());

            mailClient.sendMail(message);
            return true;
        }
        return false;
    }

    public boolean sendEmailConfirmMessage(MailClient mailClient, User user) {

        MailCode mailCode = this.createMailCode(user, MailCodeType.CONFIRM_EMAIL);

        if (this.dataStore.saveMailCode(mailCode)) {
            DefaultMessage message = new ConfirmEmailMessage();
            message.addRecipient(user.getEmail(), user.getDfvPlayer().getFirstName(), user.getFullName());

            HashMap<String, String> values = new HashMap<String, String>();
            values.put("pageName", "DFV-Turniere");
            values.put("link", this.createEmailConfirmationLink(mailCode));
            values.put("needsDfvOptIn", user.isDfvEmailOptIn() ? "0" : "1");
            message.init(values);
            mailClient.sendMail(message);
            return true;
        }
        return false;
    }

    public boolean sendEmailDfvOptInMessage(MailClient mailClient, User user, String recipientEmail) {
        MailCode mailCode = this.createMailCode(user, MailCodeType.DFV_MAIL_OPT_IN);

        if (this.dataStore.saveMailCode(mailCode)) {
            DefaultMessage optInMessage = new DfvOptInMessage();
            optInMessage.addRecipient(recipientEmail, user.getDfvPlayer().getFirstName(), user.getFullName());

            HashMap<String, String> optInValues = new HashMap<String, String>();
            optInValues.put("pageName", "DFV-Turniere");
            optInValues.put("link", this.createEmailConfirmationLink(mailCode));
            optInValues.put("registeredEmail", user.getEmail());
            optInMessage.init(optInValues);

            mailClient.sendMail(optInMessage);
            return true;
        }
        return false;
    }

    private String createEmailConfirmationLink(MailCode mailCode) {
        // TODO hardcoded language - we could use a preffered language property
        // in the user entity
        return this.websiteUrl + "de/confirm/" + mailCode.getCode();
    }

    private String createForgotPasswordLink(MailCode mailCode) {
        // TODO hardcoded language - we could use a preffered language property
        // in the user entity
        return this.websiteUrl + "de/forgot/password/" + mailCode.getCode();
    }

    public MailCode createMailCode(User user, MailCodeType type) {
        MailCode mailCode = new MailCode();

        mailCode.setUser(user);
        mailCode.setType(type);
        mailCode.setCode(this.getRandomCode());

        return mailCode;
    }

    private String getRandomCode() {
        return new BigInteger(130, this.secureRandom).toString(32);
    }
}
