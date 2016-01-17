package de.ultical.backend.app;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetup;

import de.ultical.backend.app.MailClient.UlticalMessage;
import de.ultical.backend.app.UltiCalConfig.MailConfig;

public class MailClientTest {

    private static final String SMTP_SENDER = "test@localhost.test";
    private static final int SMTP_PORT = 1025;
    private static final String SMTP_PASSWORD = "testtest";
    private static final String SMTP_USER = "test";
    private static final String SMTP_HOST = "localhost";
    private static UltiCalConfig config;
    private MailClient client;

    @Mock
    UlticalMessage testMessage;
    @Mock
    UlticalMessage testMessageTwoRec;

    @ClassRule
    public static GreenMailRule greenMail = new GreenMailRule(new ServerSetup(SMTP_PORT, SMTP_HOST, "smtp"));

    @BeforeClass
    public static void beforeClass() throws Exception {
        config = new UltiCalConfig();
        MailConfig mailConfig = new MailConfig();
        config.setMail(mailConfig);
        mailConfig.setSmtpHost(SMTP_HOST);
        mailConfig.setSmtpPort(String.valueOf(SMTP_PORT));
        mailConfig.setSmtpUser(SMTP_USER);
        mailConfig.setSmtpPassword(SMTP_PASSWORD);
        mailConfig.setSmtpSender(SMTP_SENDER);

        greenMail.setUser(SMTP_SENDER, SMTP_USER, SMTP_PASSWORD);
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        final SessionFactory sf = new SessionFactory();
        sf.config = MailClientTest.config;
        this.client = new MailClient();
        this.client.mailSession = sf.provide();
        this.client.mailSession.setDebug(true);
        this.client.config = MailClientTest.config;

        when(this.testMessage.getSubject()).thenReturn("Test from: " + MailClientTest.class.getName());
        when(this.testMessage.getRecipients())
                .thenReturn(new HashSet<String>(Collections.singletonList("test@frisbeesportverband.de")));
        when(this.testMessage.getRenderedMessage(Mockito.anyString())).thenReturn("Foo Bar");
        when(this.testMessage.getSenderName()).thenReturn("Mister Frisbee");

        when(this.testMessageTwoRec.getSubject()).thenReturn("Test from: " + MailClientTest.class.getName());
        when(this.testMessageTwoRec.getRecipients()).thenReturn(
                new HashSet<String>(Arrays.asList("test@frisbeesportverbande.de", "testtoo@frisbeesportverband.de")));
        when(this.testMessageTwoRec.getRenderedMessage(Mockito.anyString())).thenReturn("Foo Bar");
        when(this.testMessageTwoRec.getSenderName()).thenReturn("Mrs Frisbee");
    }

    @After
    public void tearDown() throws Exception {
        greenMail.reset();
    }

    @Test
    public void testSingleMessage() throws Exception {
        this.client.sendMail(this.testMessage);
        final MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages, is(notNullValue()));
        assertThat(messages.length, is(equalTo(1)));
        assertThat(messages[0].getAllRecipients(), is(notNullValue()));
        assertThat(messages[0].getAllRecipients().length, is(equalTo(1)));
        assertThat(messages[0].getAllRecipients()[0], is(equalTo(new InternetAddress("test@frisbeesportverband.de"))));
        assertThat(messages[0].getFrom()[0], is(equalTo(new InternetAddress(SMTP_SENDER))));
    }

    @Test
    public void testTwoMessages() throws Exception {
        assertThat(this.testMessage, is(notNullValue()));

        if (this.client != null) {
            this.client.sendMail(this.testMessageTwoRec);
        }

        final MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages, notNullValue());
        assertThat(messages.length, is(equalTo(2)));
    }

    @Test(expected = NullPointerException.class)
    public void testNpe() throws Exception {
        this.client.sendMail(null);
    }

}
