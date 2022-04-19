package de.ultical.backend.data.policies;

import de.ultical.backend.api.transferClasses.DfvMvPlayer;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.TournamentEdition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultPolicy implements Policy {

    private DataStore dataStore;
    private Map<String, String> parameters;

    public DefaultPolicy(DataStore dataStore) {
        this.dataStore = dataStore;
        this.parameters = new HashMap<>();
    }

    @Override
    public int addPlayerToRoster(DfvPlayer player, Roster roster) {
        // check if player is already in a roster of this season and division
        List<Roster> rostersWithPlayer = this.dataStore.getRosterByPlayerSeasonDivision(player.getId(), roster);

        if (rostersWithPlayer.size() > 0) {
            List<String> qualifiedTeamNames = rostersWithPlayer.stream()
                    .map(ro -> ro.getNameAddition().isEmpty()
                            ? ro.getTeam().getName()
                            : ro.getTeam().getName() + " " + ro.getNameAddition())
                    .collect(Collectors.toList());

            this.parameters.put("team_name", String.join(", ", qualifiedTeamNames));
            return Policy.ALREADY_IN_DIFFERENT_ROSTER;
        }

        return Policy.OK;
    }

    @Override
    public int registerRosterToEdition(Roster roster, TournamentEdition tournamentEdition) {
        // TODO Auto-generated method stub
        return Policy.OK;
    }

    @Override
    public Eligibility getPlayerEligibility(DfvMvPlayer player) {
        if (!player.isActive()) {
            return Eligibility.NOT_ACTIVE;
        } else if (!player.hasDse()) {
            return Eligibility.NO_DSE;
        } else if (player.isIdle()) {
            return Eligibility.IDLE;
        } else if (!player.isPaid()) {
            return Eligibility.NOT_PAID;
        } else {
            return Eligibility.ELIGIBLE;
        }
    }

    @Override
    public Map<String, String> getErrorParameters() {
        return this.parameters;
    }

}
