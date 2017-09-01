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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hamcrest.CoreMatchers;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

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
