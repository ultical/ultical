package de.ultical.backend.app;

import static com.jayway.restassured.RestAssured.given;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.core.MediaType;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetup;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import de.ultical.backend.utils.test.DfvMvSimulator;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardClientRule;

public class RegistrationIntegrationTest {

	public static final String REGISTER_PATH = "/command/register";
	@ClassRule
	public static DropwizardClientRule dcr = new DropwizardClientRule(new DfvMvSimulator());

	@ClassRule
	public static GreenMailRule greenMail = new GreenMailRule(new ServerSetup(5587, "localhost", "smtp"));

	public static DropwizardTestSupport<UltiCalConfig> testSupport;

	@BeforeClass
	public static void beforeClass() throws Exception {
		ConfigurationFactory<UltiCalConfig> configFactory = new ConfigurationFactory<UltiCalConfig>(UltiCalConfig.class,
				null, Jackson.newObjectMapper(), "dw");
		UltiCalConfig config = configFactory.build(new FileConfigurationSourceProvider(),
				ResourceHelpers.resourceFilePath("testConfig.yaml"));
		config.getDfvApi().setUrl(dcr.baseUri().toString());
		testSupport = new DropwizardTestSupport<UltiCalConfig>(Application.class, config);

		testSupport.before();

		RestAssured.port = testSupport.getLocalPort();
	}

	@AfterClass
	public static void after() {
		testSupport.after();
	}

	@Test
	public void testPasswordTooShort() {
		given().contentType(MediaType.APPLICATION_JSON).body(fixture("fixtures/registrationTest/shortPassword.json"))
				.accept(ContentType.JSON).post(REGISTER_PATH).then().statusCode(200).and()
				.contentType(MediaType.APPLICATION_JSON).and().body("status", Matchers.equalTo("VALIDATION_ERROR"));
	}

	@Test
	public void testUnknownName() {
		given().contentType(MediaType.APPLICATION_JSON).body(fixture("fixtures/registrationTest/unknownName.json"))
				.accept(ContentType.JSON).post(REGISTER_PATH).then().statusCode(200).and().contentType(ContentType.JSON)
				.and().body("status", Matchers.equalTo("NOT_FOUND"));
	}

	@Test
	public void testSuccessfulRegistration_unambiguous() throws Exception {
		given().contentType(ContentType.JSON).accept(ContentType.JSON)
				.body(fixture("fixtures/registrationTest/success_unambiguous.json")).post(REGISTER_PATH).then()
				.statusCode(200).and().body("status", Matchers.equalTo("SUCCESS")).and().contentType(ContentType.JSON);
		assertTrue(greenMail.waitForIncomingEmail(1));
		final MimeMessage message = greenMail.getReceivedMessages()[0];
		Address[] recipients = message.getRecipients(RecipientType.TO);
		assertNotNull(recipients);
		assertEquals(1, recipients.length);
		assertEquals("test@ultical.de", ((InternetAddress) recipients[0]).getAddress());
	}

	@Test
	public void testAmbiguousRequest() {
		given().contentType(ContentType.JSON).accept(ContentType.JSON)
				.body(fixture("fixtures/registrationTest/identicalNameAndBirthdate.json")).post(REGISTER_PATH).then()
				.statusCode(200).and().contentType(ContentType.JSON).and().body("status", Matchers.equalTo("AMBIGUOUS"))
				.and().body("clubs", Matchers.hasSize(2));
	}
}
