package de.ultical.backend.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.ultical.backend.model.Team;

public class PhaseSeeding extends Phase {

    public PhaseSeeding(String name, int numTeams) {
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

    protected void setSeeding(Map<Integer, Team> teamSeeding) {
        Map<Integer, TeamRepresentation> seeding = new HashMap<Integer, TeamRepresentation>();
        for (Entry<Integer, Team> entry : teamSeeding.entrySet()) {
            seeding.put(entry.getKey(), new TeamRepresentation(entry.getValue()));
        }
        this.outputMapping = seeding;
        this.checkCompleteness();
    }

    protected void setSeed(Integer seed, TeamRepresentation teamRep) {
        this.outputMapping.put(seed, teamRep);
        this.checkCompleteness();
    }

    private void checkCompleteness() {
        this.complete = this.outputMapping.size() == this.numTeams;
    }

    @Override
    public void finalizeCreation() {
    }

    @Override
    protected Map<Integer, TeamRepresentation> updateStandings() {
        return this.outputMapping;
    }
}
