package de.ultical.backend.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.ultical.backend.model.Team;

public class PhaseStandings extends Phase {

    public PhaseStandings(String name, int numTeams) {
        super(name, numTeams);
    }

    @Override
    public List<Round> getRounds() {
        return new ArrayList<Round>();
    }

    @Override
    public int getNumRounds() {
        return 0;
    }

    public Map<Integer, Team> getStandings() {
        this.updateStandings();

        Map<Integer, Team> standings = new HashMap<Integer, Team>();

        for (Entry<Integer, TeamRepresentation> entry : this.getInputMapping().entrySet()) {
            standings.put(entry.getKey(), entry.getValue().getTeam());
        }

        return standings;
    }

    @Override
    public void finalizeCreation() {
        // nothing to do here, there are no games to be played
    }

    @Override
    protected Map<Integer, TeamRepresentation> updateStandings() {
        this.createTeamInputMapping();
        return null;
    }

}
