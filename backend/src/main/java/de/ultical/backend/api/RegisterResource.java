package de.ultical.backend.api;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.api.transferClasses.DfvMvPlayer;
import de.ultical.backend.api.transferClasses.RegisterRequest;
import de.ultical.backend.api.transferClasses.RegisterResponse;
import de.ultical.backend.api.transferClasses.RegisterResponse.RegisterResponseStatus;
import de.ultical.backend.app.DfvApiConfig;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.Gender;
import de.ultical.backend.model.User;

/**
 * Handle new user registration
 *
 * @author bas
 *
 */
@Path("/register")
public class RegisterResource {

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
	public RegisterResponse registerRequest(RegisterRequest registerRequest) {
		// TODO check authorisation
		System.out.println("Hello Register");

		// validate data
		if (registerRequest.getPassword().length() < 8) {
			return new RegisterResponse(RegisterResponseStatus.VALIDATION_ERROR);
		}

		// check if a user with this properties exist in the dfv db
		Set<DfvMvName> names = this.dataStore.getDfvNames(registerRequest.getFirstName(), registerRequest.getLastName());

		// now we have zero or more matches
		// get each one's full information
		List<DfvMvPlayer> foundPlayers = new ArrayList<DfvMvPlayer>();

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String registerUserBirthdayString = df.format(registerRequest.getBirthDate());

		for (DfvMvName name : names) {
			WebTarget target = this.client.target(this.dfvApi.getUrl()).path("profil").path(String.valueOf(name.getDfvnr())).queryParam("token", this.dfvApi.getToken())
					.queryParam("secret", this.dfvApi.getSecret());

			Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
			DfvMvPlayer player = invocationBuilder.get(DfvMvPlayer.class);

			// find a matching birthday and email address
			if (player != null && registerUserBirthdayString.equals(player.getGeburtsdatum()) && registerRequest.getEmail().equalsIgnoreCase(player.getEmail())) {
				foundPlayers.add(player);
			}
		}

		/*
		 * we now have a set of players - most likely filled with one or none
		 * entity, however, if we encounter more than one entity we check
		 * whether the email addresses match
		 */
		if (foundPlayers.size() == 0) {
			return new RegisterResponse(RegisterResponseStatus.NOT_FOUND);
		} else if (foundPlayers.size() > 1) {
			return new RegisterResponse(RegisterResponseStatus.AMBIGUOUS);
		}

		// one exact match found
		DfvMvPlayer playerToRegister = foundPlayers.get(0);

		// create and persist User and DfvPlayer object
		DfvPlayer dfvPlayer = new DfvPlayer();
		dfvPlayer.setBirthDate(LocalDate.parse(playerToRegister.getGeburtsdatum()));
		dfvPlayer.setFirstName(registerRequest.getFirstName());
		dfvPlayer.setLastName(registerRequest.getLastName());
		dfvPlayer.setDfvNumber(playerToRegister.getDfvnr());
		dfvPlayer.setGender(
				playerToRegister.getGeschlecht().equalsIgnoreCase("m") ? Gender.MALE : playerToRegister.getGeschlecht().equalsIgnoreCase("w") ? Gender.FEMALE : Gender.NA);
		dfvPlayer.setEmail(playerToRegister.getEmail());

		User user = new User();
		user.setEmail(registerRequest.getEmail());
		user.setPassword(registerRequest.getPassword());
		user.setDfvPlayer(dfvPlayer);

		this.dataStore.storeUser(user);

		System.out.println(dfvPlayer);
		System.out.println("found: " + playerToRegister);

		// return success code
		RegisterResponse response = new RegisterResponse(RegisterResponseStatus.SUCCESS);
		response.setUser(user);

		return response;
	}

}
