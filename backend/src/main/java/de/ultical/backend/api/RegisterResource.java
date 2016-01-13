package de.ultical.backend.api;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.mindrot.jbcrypt.BCrypt;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.api.transferClasses.DfvMvPlayer;
import de.ultical.backend.api.transferClasses.RegisterRequest;
import de.ultical.backend.api.transferClasses.RegisterResponse;
import de.ultical.backend.api.transferClasses.RegisterResponse.RegisterResponseStatus;
import de.ultical.backend.app.EmailCodeService;
import de.ultical.backend.app.MailClient;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;
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

    @Inject
    private Client client;

    @Inject
    private DataStore dataStore;

    @Inject
    private UltiCalConfig config;

    @Inject
    private MailClient mailClient;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public RegisterResponse registerRequest(RegisterRequest registerRequest) {

        // validate data
        if (registerRequest.getPassword().length() < 10) {
            return new RegisterResponse(RegisterResponseStatus.VALIDATION_ERROR);
        }

        this.dataStore.setAutoCloseSession(false);

        // check if a user with this properties exist in the dfv db
        List<DfvMvName> names = this.dataStore.getDfvNames(registerRequest.getFirstName(),
                registerRequest.getLastName());

        // now we have zero or more matches
        // get each one's full information
        List<DfvMvPlayer> foundPlayers = new ArrayList<DfvMvPlayer>();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String registerUserBirthdayString = df.format(registerRequest.getBirthDate());

        for (DfvMvName name : names) {
            WebTarget target = this.client.target(this.config.getDfvApi().getUrl()).path("profil")
                    .path(String.valueOf(name.getDfvNumber())).queryParam("token", this.config.getDfvApi().getToken())
                    .queryParam("secret", this.config.getDfvApi().getSecret());

            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
            DfvMvPlayer player = invocationBuilder.get(DfvMvPlayer.class);

            // find a matching birthday
            if (player != null && registerUserBirthdayString.equals(player.getGeburtsdatum())) {
                foundPlayers.add(player);
            }
        }

        if (foundPlayers.size() == 0) {
            // no matching players found
            this.dataStore.closeSession();
            return new RegisterResponse(RegisterResponseStatus.NOT_FOUND);
        }

        /*
         * we now have a set of players - most likely filled with one entity,
         * however, if we encounter more than one entity we check whether the
         * email addresses match
         */
        if (foundPlayers.size() > 1) {
            Iterator<DfvMvPlayer> playerIterator = foundPlayers.iterator();
            boolean dfvEmailMissingForAll = true;

            while (playerIterator.hasNext()) {
                DfvMvPlayer player = playerIterator.next();

                // the dfv email adress needs to be set
                if (player.getEmail().isEmpty()) {
                    playerIterator.remove();
                } else {
                    dfvEmailMissingForAll = false;
                }

                // check if emails match
                if (!registerRequest.getEmail().equalsIgnoreCase(player.getEmail())
                        && !registerRequest.getDfvEmail().equalsIgnoreCase(player.getEmail())) {
                    playerIterator.remove();
                }
            }

            // more than one player has a correct match of firstname, lastname,
            // birthday and email (not very realistic,
            // but who knows)
            if (foundPlayers.size() > 1) {
                this.dataStore.closeSession();
                return new RegisterResponse(RegisterResponseStatus.AMBIGUOUS);
            }

            if (dfvEmailMissingForAll) {
                // none of the found players had an dfv email set
                this.dataStore.closeSession();
                return new RegisterResponse(RegisterResponseStatus.NO_DFV_EMAIL);
            }

            if (foundPlayers.size() == 0) {
                // there were players but the email did not match
                this.dataStore.closeSession();
                return new RegisterResponse(RegisterResponseStatus.EMAIL_NOT_FOUND);
            }
        }

        // one exact match found
        DfvMvPlayer playerToRegister = foundPlayers.get(0);

        // check if an email is stored at dfv database
        if (playerToRegister.getEmail().isEmpty()) {
            this.dataStore.closeSession();
            return new RegisterResponse(RegisterResponseStatus.NO_DFV_EMAIL);
        }

        // check if this dfv-player (dfvNummer) is already registered in our
        // system
        if (this.dataStore.getUserByDfvNr(playerToRegister.getDfvnr()) != null) {
            this.dataStore.closeSession();
            return new RegisterResponse(RegisterResponseStatus.USER_ALREADY_REGISTERED);
        }

        // check if this email address is already taken in the system
        if (this.dataStore.getUserByEmail(registerRequest.getEmail()) != null) {
            this.dataStore.closeSession();
            return new RegisterResponse(RegisterResponseStatus.EMAIL_ALREADY_TAKEN);
        }

        // check if the corresponding dfv player is already in the db
        DfvPlayer dfvPlayer = this.dataStore.getDfvPlayerByDfvNumber(playerToRegister.getDfvnr());

        boolean playerNewlyCreated = false;

        if (dfvPlayer == null) {
            playerNewlyCreated = true;
            // create and persist User and DfvPlayer object
            dfvPlayer = new DfvPlayer(playerToRegister);
            dfvPlayer.setFirstName(registerRequest.getFirstName());
            dfvPlayer.setLastName(registerRequest.getLastName());
        }

        dfvPlayer.setEmail(registerRequest.getEmail());

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(BCrypt.hashpw(registerRequest.getPassword(), BCrypt.gensalt(10)));
        user.setDfvPlayer(dfvPlayer);

        // send Confirmation Mail to registerRequest.getEmail()
        user.setEmailConfirmed(false);

        if (!registerRequest.getEmail().equalsIgnoreCase(playerToRegister.getEmail())) {
            // user needs to confirm his 'old' email address stored in the dfv
            // database - send her a mail
            user.setDfvEmailOptIn(false);
        } else {
            user.setDfvEmailOptIn(true);
        }

        this.dataStore.storeUser(user, playerNewlyCreated);

        EmailCodeService emailCodeService = new EmailCodeService(this.dataStore, this.config.getFrontendUrl());

        emailCodeService.sendEmailConfirmMessage(this.mailClient, user);

        if (!user.isDfvEmailOptIn()) {
            emailCodeService.sendEmailDfvOptInMessage(this.mailClient, user, playerToRegister.getEmail());
        }

        this.dataStore.closeSession();

        // return success code
        RegisterResponse response = new RegisterResponse(RegisterResponseStatus.SUCCESS);
        if (!user.isDfvEmailOptIn()) {
            // disguise email
            String[] emailParts = playerToRegister.getEmail().split("@");
            String disguisedLocalPart = "";
            for (int i = 0; i < emailParts[0].length(); i++) {
                if (emailParts[0].length() <= 3 || i >= 3) {
                    disguisedLocalPart += "*";
                } else {
                    disguisedLocalPart += emailParts[0].charAt(i);
                }
            }
            response.setDfvEmail(disguisedLocalPart + "@" + emailParts[1]);
        }

        response.setUser(user);

        return response;
    }

}
