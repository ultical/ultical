package de.ultical.backend.utils.test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.dropwizard.testing.FixtureHelpers;

@Path("/")
public class DfvMvSimulator {

	@Path("profile")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllProfiles(@QueryParam("secret") String secret, @QueryParam("token") String token) {
		return Response.ok().type(MediaType.APPLICATION_JSON).entity(FixtureHelpers.fixture("fixtures/allProfiles.json"))
				.build();
	}
	
	@GET
	@Path("profil/{dfvnr}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSingleProfile(@QueryParam("secret") String secret, @QueryParam("token") String token, @PathParam("dfvnr") Integer dfvNr) {
		try {
			final String fixture = FixtureHelpers.fixture("fixtures/profile-"+dfvNr+".json");
			return Response.ok().type(MediaType.APPLICATION_JSON).entity(fixture).build();
		} catch (IllegalArgumentException iae) {
			return Response.status(404).build();
		}
	}
	
	@Path("verbaende")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVerbaende(@QueryParam("secret") String secret, @QueryParam("token") String token) {
		return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity("[{ \"name\":\"Deutscher Frisbeesport Verband\",\"acronym\":\"DFV\",\"id\":1,\"version\":1}]").build();
	}
	
	@GET
	@Path("vereine")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVereine(@QueryParam("secret") String secret, @QueryParam("token") String token) {
		return Response.ok().type(MediaType.APPLICATION_JSON).entity("[{\"name\":\"Test-Verein 1 e.V.\",\"vereinsnr\":1,\"verband\":1},{\"name\":\"Zweiter Testverein\",\"vereinsnr\":2,\"verband\":1}]").build();
	}
}
