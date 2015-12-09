package de.ultical.backend.api;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.app.DfvApiConfig;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;

/**
 * Temporary resource to init server
 *
 * @author bas
 *
 */
@Path("/init")
public class TempInitResource {

	@Inject
	private Client client;

	private DfvApiConfig dfvApi;

	@Inject
	private DataStore dataStore;

	public TempInitResource(UltiCalConfig conf) {
		this.dfvApi = conf.getDfvApi();
	}

	@GET
	public boolean initRequest() {
		WebTarget target = this.client.target(this.dfvApi.getUrl()).path("profile").queryParam("token", this.dfvApi.getToken()).queryParam("secret", this.dfvApi.getSecret());

		Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);

		List<DfvMvName> response = invocationBuilder.get(new GenericType<List<DfvMvName>>() {
		});

		this.dataStore.refreshDfvNames(response);

		return true;
	}

}
