package de.ultical.backend.api;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;

import de.ultical.backend.app.DfvApiConfig;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.User;

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

		/* USER 1 */
		User bas = new User();
		bas.setVersion(1);
		bas.setEmail("bas@knallbude.de");
		bas.setPassword("password");
		this.dataStore.addNew(bas);

		/* USER 2 */
		User basil = new User();
		basil.setVersion(2);
		basil.setEmail("kaffeeee@trinkr.com");
		basil.setPassword("pw2");
		this.dataStore.addNew(basil);

		/* DFV MV NAME */
		// List<DfvMvName> dfvNames = new ArrayList<DfvMvName>();
		//
		// DfvMvName dfvName = new DfvMvName();
		// dfvName.setDfvnr(999);
		// dfvName.setVorname("Sebastian");
		// dfvName.setNachname("Trappi");
		// dfvName.setDse(true);
		//
		// dfvNames.add(dfvName);
		// this.dataStore.refreshDfvNames(dfvNames);

		//
		// WebTarget target =
		// this.client.target(this.dfvApi.getUrl()).path("profile").queryParam("token",
		// this.dfvApi.getToken()).queryParam("secret",
		// this.dfvApi.getSecret());
		//
		// Invocation.Builder invocationBuilder =
		// target.request(MediaType.APPLICATION_JSON);
		//
		// List<DfvMvName> response = invocationBuilder.get(new
		// GenericType<List<DfvMvName>>() {
		// });
		//
		// this.dataStore.refreshDfvNames(response);

		return true;
	}

}
