/**
 * Copyright (C) 2015-2016 ultical contributors
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 * * Apache License, version 2.0
 * * Apache Software License, version 1.0
 * * Mozilla Public License, versions 1.0, 1.1 and 2.0
 * * Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU Affero General Public License
 * version 3 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the  GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
