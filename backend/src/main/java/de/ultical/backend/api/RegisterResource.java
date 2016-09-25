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
import de.ultical.backend.model.Club;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.User;

/**
 * Handle new user registration
 *
 * @author bas
 *
 */
@Path("/command/register")
public class RegisterResource {

    @Inject
    Client client;

    @Inject
    DataStore dataStore;

    @Inject
    UltiCalConfig config;

    @Inject
    MailClient mailClient;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public RegisterResponse registerRequest(RegisterRequest registerRequest) throws Exception {

        // validate data
        if (registerRequest.getPassword().length() < 10) {
            return new RegisterResponse(RegisterResponseStatus.VALIDATION_ERROR);
        }

        try (AutoCloseable c = this.dataStore.getClosable()) {

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
                        .path(String.valueOf(name.getDfvNumber()))
                        .queryParam("token", this.config.getDfvApi().getToken())
                        .queryParam("secret", this.config.getDfvApi().getSecret());

                Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
                DfvMvPlayer player = invocationBuilder.get(DfvMvPlayer.class);

                // find a matching birthday
                if (player != null && registerUserBirthdayString.equals(player.getDobString())) {
                    foundPlayers.add(player);
                }
            }

            /*
             * we now have a set of players with matching names and birthdays
             */
            DfvMvPlayer playerToRegister = null;

            switch (foundPlayers.size()) {
            case 0:
                // no matching players found

                return new RegisterResponse(RegisterResponseStatus.NOT_FOUND);
            case 1:
                // one match - that's it
                playerToRegister = foundPlayers.get(0);
                break;
            default:
                // if we encounter more than one entity we check whether the
                // email
                // addresses match
                List<DfvMvPlayer> matchingEmailPlayers = new ArrayList<DfvMvPlayer>();
                Iterator<DfvMvPlayer> playerIterator = foundPlayers.iterator();

                while (playerIterator.hasNext()) {
                    DfvMvPlayer player = playerIterator.next();

                    // the dfv email adress needs to be set
                    if (player.getEmail().isEmpty()) {
                        playerIterator.remove();
                    }

                    // check if emails match
                    if (registerRequest.getEmail().equalsIgnoreCase(player.getEmail())) {
                        matchingEmailPlayers.add(player);
                    }
                }

                if (foundPlayers.size() == 0) {
                    // none of the found players had an dfv email set

                    return new RegisterResponse(RegisterResponseStatus.NO_DFV_EMAIL);
                }

                if (matchingEmailPlayers.size() == 1) {
                    // we found one matching email, that should be it
                    playerToRegister = matchingEmailPlayers.get(0);
                } else {
                    // it's still ambiguous, check if the user chose a club
                    if (registerRequest.getClubId() != -1) {
                        if (matchingEmailPlayers.size() > 1) {
                            for (DfvMvPlayer player : matchingEmailPlayers) {
                                if (player.getClub() == registerRequest.getClubId()) {
                                    playerToRegister = player;
                                }
                            }
                        } else {
                            for (DfvMvPlayer player : foundPlayers) {
                                if (player.getClub() == registerRequest.getClubId()) {
                                    playerToRegister = player;
                                }
                            }
                        }
                        if (playerToRegister == null) {

                            return new RegisterResponse(RegisterResponseStatus.NOT_FOUND);
                        }
                    } else {
                        // no club chosen, so let her choose one
                        RegisterResponse response;
                        List<Club> clubs = new ArrayList<Club>();

                        if (matchingEmailPlayers.size() > 1) {
                            /*
                             * more than one player has a correct match of name,
                             * birthday and email - probably the same player in
                             * different clubs - let her choose
                             */
                            // get clubs
                            for (DfvMvPlayer player : matchingEmailPlayers) {
                                Club club = this.dataStore.getClub(player.getClub());
                                if (club != null) {
                                    clubs.add(club);
                                }
                            }
                            response = new RegisterResponse(RegisterResponseStatus.AMBIGUOUS_EMAIL);
                        } else {
                            /*
                             * no matching email found so we have one or more
                             * players with matching names and birhdates
                             */
                            for (DfvMvPlayer player : foundPlayers) {
                                Club club = this.dataStore.getClub(player.getClub());
                                if (club != null) {
                                    clubs.add(club);
                                }
                            }
                            response = new RegisterResponse(RegisterResponseStatus.AMBIGUOUS);
                        }
                        response.setClubs(clubs);

                        // return choosing message
                        return response;
                    }
                }
            }

            // check if an email is stored at dfv database
            if (playerToRegister.getEmail().isEmpty()) {

                return new RegisterResponse(RegisterResponseStatus.NO_DFV_EMAIL);
            }

            // check if this dfv-player (dfvNummer) is already registered in our
            // system
            if (this.dataStore.getUserByDfvNr(playerToRegister.getDfvNumber()) != null) {

                return new RegisterResponse(RegisterResponseStatus.USER_ALREADY_REGISTERED);
            }

            // check if this email address is already taken in the system
            if (this.dataStore.getUserByEmail(registerRequest.getEmail()) != null) {

                return new RegisterResponse(RegisterResponseStatus.EMAIL_ALREADY_TAKEN);
            }

            // check if the corresponding dfv player is already in the db
            DfvPlayer dfvPlayer = this.dataStore.getDfvPlayerByDfvNumber(playerToRegister.getDfvNumber());

            boolean playerNewlyCreated = false;

            if (dfvPlayer == null) {
                playerNewlyCreated = true;
                // create and persist User and DfvPlayer object
                dfvPlayer = new DfvPlayer(playerToRegister);
                dfvPlayer.setFirstName(registerRequest.getFirstName());
                dfvPlayer.setLastName(registerRequest.getLastName());

                Club club = this.dataStore.getClub(playerToRegister.getClub());
                dfvPlayer.setClub(club);
            }

            dfvPlayer.setEmail(registerRequest.getEmail());

            User user = new User();
            user.setEmail(registerRequest.getEmail());
            user.setPassword(BCrypt.hashpw(registerRequest.getPassword(), BCrypt.gensalt(10)));
            user.setDfvPlayer(dfvPlayer);

            // send Confirmation Mail to registerRequest.getEmail()
            user.setEmailConfirmed(false);

            if (!registerRequest.getEmail().equalsIgnoreCase(playerToRegister.getEmail())) {
                // user needs to confirm his 'old' email address stored in the
                // dfv
                // database - send her a mail
                user.setDfvEmailOptIn(false);
            } else {
                user.setDfvEmailOptIn(true);
            }

            this.dataStore.storeUser(user, playerNewlyCreated);
            EmailCodeService emailCodeService = new EmailCodeService(this.dataStore, this.config.getFrontendUrl());

            emailCodeService.sendEmailConfirmMessage(this.mailClient, user);

            if (!user.isDfvEmailOptIn()) {
                // delay 1 second to give mail server time to send first message
                // (there are problems sometimes)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                emailCodeService.sendEmailDfvOptInMessage(this.mailClient, user, playerToRegister.getEmail());
            }

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

}
