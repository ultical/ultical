package de.ultical.backend.app;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hamcrest.CoreMatchers;
import org.junit.*;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Header;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;

public class SimpleIT {

	@ClassRule
	public static DropwizardAppRule<UltiCalConfig> APP = new DropwizardAppRule<UltiCalConfig>(Application.class,
			ResourceHelpers.resourceFilePath("testConfig.yaml"));

	@BeforeClass
	public static void setUpClass() throws Exception {
		// we tell RestAssured once, which port we use.
		RestAssured.port = APP.getLocalPort();
	}

	@Test
	public void testThatServerIsUpAndRunning() {
		RestAssured.when().get("/events").then().statusCode(Response.Status.OK.getStatusCode());
	}

	@Test
	public void testStoreTournament() {
		final String fixture = "{\"name\":\"Test-Turnier 2015\",\"firstDay\":\"20151003\",\"lastDay\":\"20151004\"}";
		RestAssured.given().header(new Header("Content-Type", MediaType.APPLICATION_JSON)).body(fixture)
				.post("/tournaments").then().statusCode(Response.Status.OK.getStatusCode()).and()
				.body("name", CoreMatchers.equalTo("Test-Turnier 2015"));

		// we create a second, very similar fixture for a tournament having the
		// identical name as the first one. This is supposed to result in a
		// CONFLICT http status code.
		final String similarFixture = "{\"name\":\"Test-Turnier 2015\",\"firstDay\":\"20151103\",\"lastDay\":\"20151104\"}";
		RestAssured.given().header(new Header("Content-Type", MediaType.APPLICATION_JSON)).body(similarFixture)
				.post("/tournaments").then().statusCode(Response.Status.CONFLICT.getStatusCode());

		RestAssured.when().get("/tournaments").then().statusCode(200); // we
																		// should
																		// also
																		// check
																		// that
																		// we
																		// have
																		// exactly
																		// one
																		// tournament
																		// in
																		// result
																		// list.
		
		//finally we can try to get the tournament, we saved at the start of this method back from the server
		//the nice part here is that restassured does the necessary urlencoding out of the box
		RestAssured.when().get("/tournaments/Test-Turnier 2015").then().statusCode(200);
	}

}
