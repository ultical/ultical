package de.ultical.backend.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.ultical.backend.model.Team;
import lombok.Getter;
import lombok.Setter;

public class Schedule {

    @Getter
    private int numTeams;

    @Getter
    @Setter
    private List<Phase> phases;

    @Getter
    private PhaseSeeding seedingPhase;

    public Schedule(int numTeams) {
        this.phases = new ArrayList<Phase>();
        this.numTeams = numTeams;
        this.createSeedingPhase();
    }

    private void createSeedingPhase() {
        this.seedingPhase = new PhaseSeeding("Seeding", this.numTeams);
        this.addPhase(this.seedingPhase);
    }

    public void addPhase(Phase phase) {
        this.phases.add(phase);
    }

    public List<Round> getRounds() {
        this.finalizeScheduleCreation();
        List<Round> rounds = new ArrayList<Round>();

        for (Phase phase : this.phases) {
            rounds.addAll(phase.getRounds());
        }

        // sort rounds by timing index
        Collections.sort(rounds, new Comparator<Round>() {
            @Override
            public int compare(Round round1, Round round2) {
                return Integer.compare(round1.getTimingIndex(), round2.getTimingIndex());
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
        for (Phase phase : this.phases) {
            if (phase instanceof PhaseStandings) {
                return ((PhaseStandings) phase).getStandings();
            }
        }
        return null;
    }

    public boolean isComplete() {
        for (Phase phase : this.phases) {
            if (phase instanceof PhaseStandings) {
                return phase.isComplete();
            }
        }
        return false;
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
        if (this.seedingPhase.timingIndex < 0) {
            this.doPhaseTiming(this.seedingPhase);
        }

        for (Phase phase : this.phases) {
            phase.finalizeCreation();
        }
    }

    private void doPhaseTiming(Phase phase) {
        if (phase.timingIndex < 0) {
            phase.timingIndex = 0;
        }

        for (PhaseAdapter outgoingAdapter : phase.outgoingAdapters) {
            Phase nextPhase = outgoingAdapter.getNextPhase();
            if (nextPhase.timingIndex < phase.timingIndex) {
                nextPhase.timingIndex = phase.timingIndex + 1;
            }
            this.doPhaseTiming(nextPhase);
        }
    }

    public void updateSchedule() {
        // TODO: do this also
    }
}
