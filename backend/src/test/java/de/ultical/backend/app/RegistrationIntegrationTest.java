package de.ultical.backend.app;

import javax.ws.rs.core.MediaType;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.jayway.restassured.RestAssured;

import de.ultical.backend.utils.test.DfvMvSimulator;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.FixtureHelpers;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardClientRule;

public class RegistrationIntegrationTest {

	public static final String REGISTER_PATH = "/command/register";
	@ClassRule
	public static DropwizardClientRule dcr = new DropwizardClientRule(new DfvMvSimulator());
	
	public static DropwizardTestSupport<UltiCalConfig> testSupport;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		ConfigurationFactory<UltiCalConfig> configFactory = new ConfigurationFactory<UltiCalConfig>(UltiCalConfig.class, null, Jackson.newObjectMapper(), "dw");
		UltiCalConfig config = configFactory.build(new FileConfigurationSourceProvider(),ResourceHelpers.resourceFilePath("testConfig.yaml"));
		config.getDfvApi().setUrl(dcr.baseUri().toString());
		testSupport = new DropwizardTestSupport<UltiCalConfig>(Application.class, config);
		
		testSupport.before();
	}
	
	@AfterClass
	public static void after() {
		testSupport.after();
	}

	@Test
	public void test() {
		RestAssured.port = testSupport.getLocalPort();
		RestAssured.given().contentType(MediaType.APPLICATION_JSON)
				.body(FixtureHelpers.fixture("fixtures/registrationTest/shortPassword.json")).post(REGISTER_PATH).then()
				.statusCode(200).and().contentType(MediaType.APPLICATION_JSON).and()
				.body("status", Matchers.equalTo("VALIDATION_ERROR"));
	}

}
