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
                        DfvMvName mvName = this.dataStore.getDfvMvName(player.getDfvNumber());
                        DfvMvPlayer mvPlayer = this.getMvPlayer(player);
                        if (mvPlayer != null) {
                            this.updatePlayer(player, mvName, mvPlayer);
                            LOGGER.debug(
                                    "Updated player (id={}) to the following values: firstName={}, lastName={}, lastModified={}, active={}, gender={}, birthDate={}, email={}",
                                    player.getId(), player.getFirstName(), player.getLastName(),
                                    player.getLastModified(), player.isActive(), player.getGender(),
                                    player.getBirthDate(), player.getEmail());
                        } else {
                            // for some reason we did not find a matching
                            // player, so we deactivate the player we have
                            LOGGER.warn(
                                    "We got a player in our DB with id={}, dfvnumber={} that could not be loaded from the dfv-mv database!",
                                    player.getId(), player.getDfvNumber());
                            player.setActive(false);
                            player.setLastModified(LocalDateTime.now());
                        }
                        this.dataStore.update(player);
                        LOGGER.debug("stored updated player in db");
                    }
                }
            }

            return true;
        }
    }

    private void updatePlayer(DfvPlayer player, DfvMvName mvName, DfvMvPlayer mvPlayer) {
        player.setFirstName(mvName.getFirstName());
        player.setLastName(mvName.getLastName());
        player.setLastModified(LocalDateTime.ofInstant(mvName.getLastModified().toInstant(), ZoneId.systemDefault()));
        player.setActive(mvName.isActive());
        player.setGender(Gender.robustValueOf(mvPlayer.getGeschlecht()));
        player.setBirthDate(LocalDate.parse(mvPlayer.getGeburtsdatum()));
        player.setGender(Gender.robustValueOf(mvPlayer.getGeschlecht()));
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
