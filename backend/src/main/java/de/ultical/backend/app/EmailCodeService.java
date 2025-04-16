package de.ultical.backend.app;

import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.HashMap;

import de.ultical.backend.app.mail.ConfirmEmailMessage;
import de.ultical.backend.app.mail.DfvOptInMessage;
import de.ultical.backend.app.mail.ForgotPasswordMessage;
import de.ultical.backend.app.mail.SystemMessage;
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
            try {
                HashMap<String, String> values = new HashMap<String, String>();
                values.put("link", this.createForgotPasswordLink(mailCode));
                SystemMessage message = new ForgotPasswordMessage(values);
                message.addRecipient(user.getEmail(), user.getDfvPlayer().getFirstName(), user.getFullName());

                mailClient.sendMail(message);
            } catch (URISyntaxException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean sendEmailConfirmMessage(MailClient mailClient, User user) {

        MailCode mailCode = this.createMailCode(user, MailCodeType.CONFIRM_EMAIL);

        if (this.dataStore.saveMailCode(mailCode)) {
            try {
                SystemMessage message = new ConfirmEmailMessage();
                message.addRecipient(user.getEmail(), user.getDfvPlayer().getFirstName(), user.getFullName());

                HashMap<String, String> values = new HashMap<String, String>();
                values.put("pageName", "DFV-Turniere");
                values.put("link", this.createEmailConfirmationLink(mailCode));
                values.put("needsDfvOptIn", user.isDfvEmailOptIn() ? "0" : "1");
                message.init(values);
                mailClient.sendMail(message);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public boolean sendEmailDfvOptInMessage(MailClient mailClient, User user, String recipientEmail) {
        MailCode mailCode = this.createMailCode(user, MailCodeType.DFV_MAIL_OPT_IN);

        if (this.dataStore.saveMailCode(mailCode)) {
            try {
                SystemMessage optInMessage = new DfvOptInMessage();
                optInMessage.addRecipient(recipientEmail, user.getDfvPlayer().getFirstName(), user.getFullName());

                HashMap<String, String> optInValues = new HashMap<String, String>();
                optInValues.put("pageName", "DFV-Turniere");
                optInValues.put("link", this.createEmailConfirmationLink(mailCode));
                optInValues.put("registeredEmail", user.getEmail());
                optInMessage.init(optInValues);

                mailClient.sendMail(optInMessage);
                return true;
            } catch (URISyntaxException e) {
                return false;
            }
        }
        return false;
    }

    private String createEmailConfirmationLink(MailCode mailCode) throws URISyntaxException {
        // TODO hardcoded language - we could use a preffered language property
        // in the user entity
        return buildUrl(websiteUrl, "de/confirm/" + mailCode.getCode());
    }

    private String createForgotPasswordLink(MailCode mailCode) throws URISyntaxException {
        // TODO hardcoded language - we could use a preffered language property
        // in the user entity
        return buildUrl(this.websiteUrl, "de/forgot/password/" + mailCode.getCode());
    }

    protected String buildUrl(String baseUrl, String path) throws URISyntaxException {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new URISyntaxException(baseUrl, "Base URL is empty");
        }
        if (path == null || path.isEmpty()) {
            return baseUrl;
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return baseUrl + path;
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
