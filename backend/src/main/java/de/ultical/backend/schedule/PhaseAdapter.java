package de.ultical.backend.schedule;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Data;

@Data
public class PhaseAdapter {

    private Phase previousPhase;
    private Phase nextPhase;
    private Map<Integer, Integer> teamMapping;

    public PhaseAdapter() {
        this.teamMapping = new HashMap<Integer, Integer>();
    }

    public void addMapping(Integer positionPreviousPhase, Integer positionNextPhase) {
        this.teamMapping.put(positionPreviousPhase, positionNextPhase);
    }

    public void setPreviousPhase(Phase phase) {
        this.previousPhase = phase;
        phase.addOutgoingAdapter(this);
    }

    public void setNextPhase(Phase phase) {
        this.nextPhase = phase;
        phase.addIncomingAdapter(this);
    }

    public Map<Integer, TeamRepresentation> getNextTeamMapping() {
        Map<Integer, TeamRepresentation> orderedTeams = new HashMap<Integer, TeamRepresentation>();

        // update previous phase
        this.previousPhase.updateStandings();

        for (Entry<Integer, Integer> mapping : this.teamMapping.entrySet()) {
            orderedTeams.put(mapping.getValue(), this.previousPhase.getTeamByResult(mapping.getKey()));
        }

        return orderedTeams;
    }

    @Override
    public String toString() {
        return "Prev Phase " + this.previousPhase + " - Next Phase " + this.nextPhase;
    }
}
