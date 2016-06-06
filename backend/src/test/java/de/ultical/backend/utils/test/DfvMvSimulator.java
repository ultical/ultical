package de.ultical.backend.utils.test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.dropwizard.testing.FixtureHelpers;

public class DfvMvSimulator {

	@Path("/profiles")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllProfiles(@QueryParam("secret") String secret, @QueryParam("token") String token) {
		return Response.ok().type(MediaType.APPLICATION_JSON).entity(FixtureHelpers.fixture("allProfiles.json"))
				.build();
	}
}
