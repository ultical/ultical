package de.ultical.backend.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.ultical.backend.model.DivisionRegistration;
import de.ultical.backend.model.Team;
import lombok.Data;

@Data
public class Schedule {

    private int numTeams;
    private List<Phase> phases;
    private PhaseSeeding seedingPhase;
    private PhaseStandings standingPhase;

    // TODO: implement qualifications based on results
    private Map<Integer, DivisionRegistration> qualifications;

    public Schedule(int numTeams) {
        this.phases = new ArrayList<Phase>();
        this.numTeams = numTeams;
        this.standingPhase = null;
        this.createSeedingPhase();
        this.createStandingPhase();
    }

    private void createSeedingPhase() {
        this.seedingPhase = new PhaseSeeding("Seeding", this.numTeams);
        this.addPhase(this.seedingPhase);
    }

    private void createStandingPhase() {
        this.standingPhase = new PhaseStandings("Standings", this.numTeams);
        this.addPhase(this.standingPhase);
    }

    public void addPhase(Phase phase) {
        this.phases.add(phase);
    }

    public List<Round> getRounds() {
        // this.finalizeScheduleCreation();
        List<Round> rounds = new ArrayList<Round>();

        for (Phase phase : this.phases) {
            rounds.addAll(phase.getRounds());
        }

        // sort rounds by timing index
        Collections.sort(rounds, new Comparator<Round>() {
            @Override
            public int compare(Round round1, Round round2) {
                return Integer.compare(round1.getPhaseTimingIndex(), round2.getPhaseTimingIndex());
            }
        });
        return rounds;
    }

    public List<Game> getGames() {
        List<Game> games = new ArrayList<Game>();
        for (Round round : this.getRounds()) {
            games.addAll(round.getGames());
        }
        return games;
    }

    public Map<Integer, Team> getStandings() {
        return this.standingPhase.getStandings();
    }

    public boolean isComplete() {
        return this.standingPhase.isComplete();
    }

    public void setSeeding(Map<Integer, Team> teamSeeding) {
        this.seedingPhase.setSeeding(teamSeeding);
    }

    public void setSeed(Integer seed, Team team) {
        this.seedingPhase.setSeed(seed, new TeamRepresentation(team));
    }

    public void setSeed(Integer seed, TeamRepresentation teamRep) {
        this.seedingPhase.setSeed(seed, teamRep);
    }

    /**
     * After schedule creation checks its consistency and attaches timing
     * information to phases
     *
     * @throws Exception
     */
    public void finalizeScheduleCreation() {
        if (this.seedingPhase.getTimingIndex() < 0) {
            this.doPhaseTiming(this.seedingPhase);
        }

        for (Phase phase : this.phases) {
            phase.finalizeCreation();
        }
    }

    private void doPhaseTiming(Phase phase) {
        if (phase.getTimingIndex() < 0) {
            phase.setTimingIndex(0);
        }

        for (PhaseAdapter outgoingAdapter : phase.getOutgoingAdapters()) {
            Phase nextPhase = outgoingAdapter.getNextPhase();
            if (nextPhase.getTimingIndex() < phase.getTimingIndex()) {
                nextPhase.setTimingIndex(phase.getTimingIndex() + 1);
            }
            this.doPhaseTiming(nextPhase);
        }
    }

    public void updateSchedule() {
        this.standingPhase.updateStandings();
    }
}
