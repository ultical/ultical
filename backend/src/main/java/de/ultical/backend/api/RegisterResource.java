package de.ultical.backend.api;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import de.ultical.backend.app.DfvApiConfig;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.ApiDfvMvName;
import de.ultical.backend.model.ApiDfvMvPlayer;
import de.ultical.backend.model.ApiRegisterRequest;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.User;

/**
 * Handle new user registration
 *
 * @author bas
 *
 */
@Path("/register")
public class RegisterResource {

	public enum RegisterResponseStatus {
		SUCCESS, NOT_FOUND, VALIDATION_ERROR, AMBIGUOUS;
	}

	@Inject
	private Client client;

	@Inject
	private DataStore dataStore;

	private DfvApiConfig dfvApi;

	public RegisterResource(UltiCalConfig conf) {
		this.dfvApi = conf.getDfvApi();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public RegisterResponseStatus apiRegisterRequest(ApiRegisterRequest apiRegisterRequest) {
		// TODO check authorisation
		System.out.println("Hello Register");

		// validate data
		if (apiRegisterRequest.getPassword().length() < 8) {
			return RegisterResponseStatus.VALIDATION_ERROR;
		}

		// TODO check if a user with this properties exist in the dfv db
		Set<ApiDfvMvName> names = this.dataStore.getDfvNames(apiRegisterRequest.getFirstName(), apiRegisterRequest.getLastName());

		if (names.size() == 0) {
			// no matches found
			return RegisterResponseStatus.NOT_FOUND;
		}

		// now we have one or more matches
		// get each one's full information
		List<ApiDfvMvPlayer> players = new ArrayList<ApiDfvMvPlayer>();

		// find a matching birthday
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String registerUserBirthdayString = df.format(apiRegisterRequest.getBirthDate());

		for (ApiDfvMvName name : names) {
			WebTarget target = this.client.target(this.dfvApi.getUrl()).path("profil").path(String.valueOf(name.getDfvnr())).queryParam("token", this.dfvApi.getToken())
					.queryParam("secret", this.dfvApi.getSecret());

			Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
			ApiDfvMvPlayer player = invocationBuilder.get(ApiDfvMvPlayer.class);

			System.out.println("Matching player " + player);
			if (player != null && registerUserBirthdayString.equals(player.getGeburtsdatum())) {
				players.add(player);
			}
		}

		/*
		 * we now have a set of players - most likely filled with one or none
		 * entity however, if we encounter more than one entity we check whether
		 * the email addresses match
		 */
		if (players.size() > 1) {
			return RegisterResponseStatus.AMBIGUOUS;
		} else if (players.size() == 0) {
			return RegisterResponseStatus.NOT_FOUND;
		}

		ApiDfvMvPlayer playerToRegister = players.get(0);
		// TODO if so, create and persist User and DfvPlayer object

		User user = new User();
		user.setEmail(apiRegisterRequest.getEmail());
		user.setPassword(apiRegisterRequest.getPassword());

		DfvPlayer dfvPlayer = new DfvPlayer();
		dfvPlayer.setBirthDate(apiRegisterRequest.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
		System.out.println(dfvPlayer);
		System.out.println("found: " + playerToRegister);
		// return success code
		return RegisterResponseStatus.SUCCESS;

	}

}
