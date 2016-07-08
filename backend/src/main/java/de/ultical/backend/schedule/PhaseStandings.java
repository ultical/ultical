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
        this.createTeamInputMapping();

        Map<Integer, Team> standings = new HashMap<Integer, Team>();

        for (Entry<Integer, TeamRepresentation> entry : this.inputMapping.entrySet()) {
            standings.put(entry.getKey(), entry.getValue().getTeam());
        }

        return standings;
    }

    @Override
    public void finalizeCreation() {
        // TODO Auto-generated method stub
    }

    @Override
    protected Map<Integer, TeamRepresentation> updateStandings() {
        // TODO Auto-generated method stub
        return null;
    }

}
