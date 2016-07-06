package de.ultical.backend.jobs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.api.transferClasses.DfvMvPlayer;
import de.ultical.backend.app.MailClient;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.app.mail.SystemMessage;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Club;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.Gender;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.User;

public class DfvProfileLoader {

    private final static Logger LOGGER = LoggerFactory.getLogger(DfvProfileLoader.class);
    @Inject
    Client client;

    @Inject
    UltiCalConfig config;

    @Inject
    DataStore dataStore;
    
    @Inject
    MailClient mailClient;

    public boolean getDfvMvNames() throws Exception {

        if (!this.config.getJobs().isDfvMvSyncEnabled()) {
            return false;
        }

        try (AutoCloseable c = this.dataStore.getClosable()) {

            WebTarget target = this.client.target(this.config.getDfvApi().getUrl()).path("profile/sparte/ultimate")
                    .queryParam("token", this.config.getDfvApi().getToken())
                    .queryParam("secret", this.config.getDfvApi().getSecret());

            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);

            List<DfvMvName> response = invocationBuilder.get(new GenericType<List<DfvMvName>>() {
            });

            if (response != null) {
                response.forEach(dfvMvName -> {
                    dfvMvName.setFirstName(dfvMvName.getFirstName().trim());
                    dfvMvName.setLastName(dfvMvName.getLastName().trim());
                });

                this.dataStore.refreshDfvNames(response);
                List<DfvPlayer> playersToUpdate = this.dataStore.getPlayersToUpdate();
                if (playersToUpdate != null) {
                    for (DfvPlayer player : playersToUpdate) {
                        this.updatePlayerData(player);
                        this.validateRosterParticipation(player);
                    }
                }
            }

            return true;
        }
    }

    private void validateRosterParticipation(DfvPlayer updatedPlayer) {
        if (!updatedPlayer.isEligible()) {

        	List<Roster> rosters = dataStore.getRosterForPlayer(updatedPlayer);
        	final LocalDateTime now = LocalDateTime.now();
        	for (Roster roster : rosters) {
        		List<LocalDate> blockingDate = dataStore.getRosterBlockingDates(roster.getId());
        		if (blockingDate == null || blockingDate.isEmpty() || blockingDate.stream().allMatch(d -> d.isAfter(now.toLocalDate()))) {
        			//we either have no blocking-date for the roster or the blocking-date is later in time, thus we can safely removve the player from the roster.
        			dataStore.removePlayerFromRoster(updatedPlayer.getId(), roster.getId());
        			
        			StringBuilder sb = new StringBuilder();
    				sb.append("Der Spieler ").append(updatedPlayer.getFirstName()).append(' ').append(updatedPlayer.getLastName()).append(" (Dfv-Nummer: ").append(updatedPlayer.getDfvNumber()).append(')');
    				sb.append(" wurde aus dem Roster für die Saison ").append(roster.getSeason().getYear()).append(' ').append(roster.getSeason().getSurface()).append(' ').append(roster.getDivisionType()).append(' ');
    				if (roster.getNameAddition() != null && !roster.getNameAddition().isEmpty()) {
    					sb.append(roster.getNameAddition());
    					sb.append(' ');
    					
    				}
    				sb.append("entfernt, da er die Vorraussetzungen für eine Spielberechtigung im DFV nicht mehr erfüllt.");
    				String firstParagraph = sb.toString();
    				String explainParagraph = "Die Gründe dafür können sein:\n\tDer Spieler wurde von seinem Verein noch nicht für das nächste Kalenderjahr gemeldet\n\tDer Spieler hat seine Datenschutzerklärung zurück gezogen.";
    				
        			for (User admin : roster.getTeam().getAdmins()) {
        				SystemMessage sm = new SystemMessage();
        				sm.addParagraph(firstParagraph);
        				sm.addParagraph(explainParagraph);
        				sm.addRecipient(admin.getEmail(), admin.getDfvPlayer().getFirstName(), admin.getDfvPlayer().getFirstName() + " " + admin.getDfvPlayer().getLastName());
        				sm.setSubject("Spieler aus Roster entfernt");
        				mailClient.sendMail(sm);
        			}
        		}
        		// if blocking-date lies in the past, we should not removve the player from roster but ensure that the player cannot be added to any future rosters. This is done by checking the eligible flag.
        		// the rare-case the a player has to be remove from a roster while the season, for which the roster is used, is still in use could NOT be handled using our current data-model. However, I really doubt that this case will happen to us.
        	}
        }
    }

    private void updatePlayerData(DfvPlayer updatedPlayer) {
        DfvMvName mvName = this.dataStore.getDfvMvName(updatedPlayer.getDfvNumber());
        DfvMvPlayer mvPlayer = this.getMvPlayer(updatedPlayer);
        if (mvName != null && mvPlayer != null) {
            this.updatePlayer(updatedPlayer, mvName, mvPlayer);
            LOGGER.debug(
                    "Updated player (id={}) to the following values: firstName={}, lastName={}, lastModified={}, eligible={}, gender={}, birthDate={}, email={}",
                    updatedPlayer.getId(), updatedPlayer.getFirstName(), updatedPlayer.getLastName(),
                    updatedPlayer.getLastModified(), updatedPlayer.isEligible(), updatedPlayer.getGender(),
                    updatedPlayer.getBirthDate(), updatedPlayer.getEmail());
        } else {
            // for some reason we did not find a matching
            // player, so we deactivate the player we have
            LOGGER.debug(
                    "Deactivated player in our DB with id={}, dfvnumber={} that could not be loaded from the dfv-mv database!",
                    updatedPlayer.getId(), updatedPlayer.getDfvNumber());
            updatedPlayer.setEligible(false);
            // set modified datetime to now - 1 hour to prevent racing
            // conditions if it is re-activated right now
            updatedPlayer.setLastModified(LocalDateTime.now().minusHours(1));
        }
        this.dataStore.updateDfvPlayer(updatedPlayer);
        LOGGER.debug("stored updated player in db");
    }

    private void updatePlayer(DfvPlayer player, DfvMvName mvName, DfvMvPlayer mvPlayer) {
        player.setFirstName(mvName.getFirstName());
        player.setLastName(mvName.getLastName());
        player.setLastModified(mvName.getLastModified());

        // eligible should include active, dse and !idle
        player.setEligible(mvPlayer.isActive() && mvPlayer.hasDse() && !mvPlayer.isIdle());

        player.setGender(Gender.robustValueOf(mvPlayer.getGender()));
        player.setBirthDate(LocalDate.parse(mvPlayer.getDobString()));
        player.setEmail(mvPlayer.getEmail());

        Club club = new Club();
        club.setId(mvPlayer.getClub());
        player.setClub(club);
    }

    private DfvMvPlayer getMvPlayer(DfvPlayer player) {
        /*
         * Would be great if the WebTarget could be saved as a template ...
         */
        WebTarget playerProfilTarget = this.client.target(this.config.getDfvApi().getUrl()).path("profil")
                .path(String.valueOf(player.getDfvNumber())).queryParam("token", this.config.getDfvApi().getToken())
                .queryParam("secret", this.config.getDfvApi().getSecret());
        Invocation.Builder playerInvocationBuilder = playerProfilTarget.request(MediaType.APPLICATION_JSON);

        DfvMvPlayer mvPlayer = null;

        try {
            mvPlayer = playerInvocationBuilder.get(DfvMvPlayer.class);
        } catch (NotFoundException e) {
            return null;
        }

        return mvPlayer;
    }

}
