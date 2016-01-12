package de.ultical.backend.app;

import java.util.Collections;
import java.util.Set;

import com.codahale.metrics.health.HealthCheck;

import de.ultical.backend.app.MailClient.UlticalMessage;

public class MailHealthCheck extends HealthCheck {
    @Override
    protected Result check() throws Exception {
        UlticalMessage message = new UlticalMessage() {

            @Override
            public String getSubject() {
                return "[ultical] helth check message";
            }

            @Override
            public String getRenderedMessage(String recipient) {
                StringBuilder sb = new StringBuilder();
                sb.append("Hallo zusammen,").append('\n');
                sb.append('\n');
                sb.append(
                        "Wenn ihr diese Mail empfangt, dann funktionert der Mailverand aus ultical über die vom DFV angegebene Adresse.")
                        .append('\n');
                sb.append("Juchu!").append('\n');
                sb.append('\n');
                sb.append("Viele Grüße,").append('\n');
                sb.append("ultical");
                return sb.toString();
            }

            @Override
            public Set<String> getRecipients() {
                return Collections.singleton("team@ultical.com");
            }

            @Override
            public String getSenderName() {
                // TODO Auto-generated method stub
                return null;
            }
        };
        MailClient mailClient = ServiceLocatorProvider.INSTANCE.getServiceLocator()
                .createAndInitialize(MailClient.class);
        mailClient.sendMail(message);
        return Result.healthy("At least we did not receive an exception, when sending mails!");
    }
}