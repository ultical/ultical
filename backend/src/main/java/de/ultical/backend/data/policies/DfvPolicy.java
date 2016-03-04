package de.ultical.backend.data.policies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.TeamRegistration;
import de.ultical.backend.model.TournamentEdition;

public class DfvPolicy implements Policy {

    private DataStore dataStore;
    private Map<String, String> parameters;

    public DfvPolicy(DataStore dataStore) {
        this.dataStore = dataStore;
        this.parameters = new HashMap<String, String>();
    }

    @Override
    public int addPlayerToRoster(DfvPlayer player, Roster roster) {
        // check if player is already in a roster of this season and division
        List<Roster> rostersWithPlayer = this.dataStore.getRosterByPlayerSeasonDivision(player.getId(), roster);

        if (rostersWithPlayer.size() > 0) {
            // we found this player already in rosters
            // for each roster we check if it was registered for a tournament
            // where the team did not qualify - this would release the roster
            List<String> qualifiedTeamNamesWithPlayer = new ArrayList<String>();

            for (Roster rosterWithPlayer : rostersWithPlayer) {
                List<TeamRegistration> teamRegistrations = this.dataStore
                        .getTeamRegistrationsByRoster(rosterWithPlayer);

                boolean didQualify = true;
                for (TeamRegistration teamRegistration : teamRegistrations) {
                    if (teamRegistration.isNotQualified()) {
                        didQualify = false;
                    }
                }

                if (didQualify) {
                    String teamName = rosterWithPlayer.getTeam().getName();
                    if (!rosterWithPlayer.getNameAddition().isEmpty()) {
                        teamName += " " + rosterWithPlayer.getNameAddition();
                    }
                    qualifiedTeamNamesWithPlayer.add(teamName);
                }
            }

            if (qualifiedTeamNamesWithPlayer.size() > 0) {
                this.parameters.put("team_name", String.join(", ", qualifiedTeamNamesWithPlayer));
                return Policy.ALREADY_IN_DIFFERENT_ROSTER;
            }
        }

        return Policy.OK;
    }

    @Override
    public int registerRosterToEdition(Roster roster, TournamentEdition tournamentEdition) {
        // TODO Auto-generated method stub
        return Policy.OK;
    }

    @Override
    public Map<String, String> getErrorParameters() {
        return this.parameters;
    }

}
