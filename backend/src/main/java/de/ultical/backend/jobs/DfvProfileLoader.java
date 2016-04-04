package de.ultical.backend.jobs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.api.transferClasses.DfvMvPlayer;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.Gender;

public class DfvProfileLoader {

    private final static Logger LOGGER = LoggerFactory.getLogger(DfvProfileLoader.class);
    @Inject
    Client client;

    @Inject
    UltiCalConfig config;

    @Inject
    DataStore dataStore;

    public boolean getDfvMvNames() throws Exception {

        if (!this.config.getJobs().isDfvMvSyncEnabled()) {
            return false;
        }

        try (AutoCloseable c = this.dataStore.getClosable()) {

            WebTarget target = this.client.target(this.config.getDfvApi().getUrl()).path("profile")
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
        if (updatedPlayer.isEligible()) {

        }
    }

    private void updatePlayerData(DfvPlayer updatedPlayer) {
        DfvMvName mvName = this.dataStore.getDfvMvName(updatedPlayer.getDfvNumber());
        DfvMvPlayer mvPlayer = this.getMvPlayer(updatedPlayer);
        if (mvPlayer != null) {
            this.updatePlayer(updatedPlayer, mvName, mvPlayer);
            LOGGER.debug(
                    "Updated player (id={}) to the following values: firstName={}, lastName={}, lastModified={}, eligible={}, gender={}, birthDate={}, email={}",
                    updatedPlayer.getId(), updatedPlayer.getFirstName(), updatedPlayer.getLastName(),
                    updatedPlayer.getLastModified(), updatedPlayer.isEligible(), updatedPlayer.getGender(),
                    updatedPlayer.getBirthDate(), updatedPlayer.getEmail());
        } else {
            // for some reason we did not find a matching
            // player, so we deactivate the player we have
            LOGGER.warn(
                    "We got a player in our DB with id={}, dfvnumber={} that could not be loaded from the dfv-mv database!",
                    updatedPlayer.getId(), updatedPlayer.getDfvNumber());
            updatedPlayer.setEligible(false);
            updatedPlayer.setLastModified(LocalDateTime.now());
        }
        this.dataStore.update(updatedPlayer);
        LOGGER.debug("stored updated player in db");
    }

    private void updatePlayer(DfvPlayer player, DfvMvName mvName, DfvMvPlayer mvPlayer) {
        player.setFirstName(mvName.getFirstName());
        player.setLastName(mvName.getLastName());
        player.setLastModified(LocalDateTime.ofInstant(mvName.getLastModified().toInstant(), ZoneId.systemDefault()));

        // TODO: eligible should include active, dse and !idle
        player.setEligible(mvPlayer.isActive() && mvPlayer.hasDse() && !mvPlayer.isIdle());

        player.setGender(Gender.robustValueOf(mvPlayer.getGender()));
        player.setBirthDate(LocalDate.parse(mvPlayer.getDobString()));
        player.setEmail(mvPlayer.getEmail());
    }

    private DfvMvPlayer getMvPlayer(DfvPlayer player) {
        /*
         * Would be great if the WebTarget could be saved as a template ...
         */
        WebTarget playerProfilTarget = this.client.target(this.config.getDfvApi().getUrl()).path("profil")
                .path(String.valueOf(player.getDfvNumber())).queryParam("token", this.config.getDfvApi().getToken())
                .queryParam("secret", this.config.getDfvApi().getSecret());
        Invocation.Builder playerInvocationBuilder = playerProfilTarget.request(MediaType.APPLICATION_JSON);
        DfvMvPlayer mvPlayer = playerInvocationBuilder.get(DfvMvPlayer.class);
        return mvPlayer;
    }

}
