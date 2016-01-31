package de.ultical.backend.app.dfv;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
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
import de.ultical.backend.app.RegistrationException;
import de.ultical.backend.app.RegistrationHandler;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Club;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.User;

public class DfvRegistrationHandler implements RegistrationHandler {

    @Inject
    DataStore dataStore;
    @Inject
    UltiCalConfig config;
    @Inject
    Client client;
    @Inject
    MailClient mailClient;

    // TODO refactor this beast of a method
    @Override
    public User registerPlayer(RegisterRequest registerRequest) throws RegistrationException {

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
                if (player != null && registerUserBirthdayString.equals(player.getGeburtsdatum())) {
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

                throw new RegistrationException("player not found", RegisterResponseStatus.NOT_FOUND);
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

                    throw new RegistrationException("no dfv mail", RegisterResponseStatus.NO_DFV_EMAIL);
                }

                if (matchingEmailPlayers.size() == 1) {
                    // we found one matching email, that should be it
                    playerToRegister = matchingEmailPlayers.get(0);
                } else {
                    // it's still ambiguous, check if the user chose a club
                    if (registerRequest.getClubId() != -1) {
                        if (matchingEmailPlayers.size() > 1) {
                            for (DfvMvPlayer player : matchingEmailPlayers) {
                                if (player.getVerein() == registerRequest.getClubId()) {
                                    playerToRegister = player;
                                }
                            }
                        } else {
                            for (DfvMvPlayer player : foundPlayers) {
                                if (player.getVerein() == registerRequest.getClubId()) {
                                    playerToRegister = player;
                                }
                            }
                        }
                        if (playerToRegister == null) {

                            throw new RegistrationException("player not found", RegisterResponseStatus.NOT_FOUND);
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
                                Club club = this.dataStore.getClub(player.getVerein());
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
                                Club club = this.dataStore.getClub(player.getVerein());
                                if (club != null) {
                                    clubs.add(club);
                                }
                            }
                            response = new RegisterResponse(RegisterResponseStatus.AMBIGUOUS);
                        }
                        response.setClubs(clubs);

                        // return choosing message
                        throw new RegistrationException("ambiguous clubs", RegisterResponseStatus.AMBIGUOUS, response);

                    }
                }
            }
            // check if an email is stored at dfv database
            if (playerToRegister.getEmail().isEmpty()) {

                throw new RegistrationException("no email", RegisterResponseStatus.NO_DFV_EMAIL);
            }
            // check if this dfv-player (dfvNummer) is already registered in our
            // system
            if (this.dataStore.getUserByDfvNr(playerToRegister.getDfvnr()) != null) {

                throw new RegistrationException("already registered", RegisterResponseStatus.USER_ALREADY_REGISTERED);
            }
            // check if this email address is already taken in the system
            if (this.dataStore.getUserByEmail(registerRequest.getEmail()) != null) {

                throw new RegistrationException("email already taken", RegisterResponseStatus.EMAIL_ALREADY_TAKEN);
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

                Club club = this.dataStore.getClub(playerToRegister.getVerein());
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
                emailCodeService.sendEmailDfvOptInMessage(this.mailClient, user, playerToRegister.getEmail());
            }
            return user;
        } catch (Exception e) {
            // god damn declared exception at AutoCloseable.close()...
            throw new RuntimeException(e);
        }
    }

}
