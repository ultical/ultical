package de.ultical.backend.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public abstract class Phase {

    protected int numTeams;
    private boolean complete = false;

    private int timingIndex = -1;
    private PhaseOptions options;
    private String name;
    private List<Round> rounds;

    private List<PhaseAdapter> incomingAdapters;
    private List<PhaseAdapter> outgoingAdapters;

    private Map<Integer, TeamRepresentation> inputMapping;
    private Map<Integer, TeamRepresentation> outputMapping;

    public Phase(String name, int numTeams) {
        this.numTeams = numTeams;
        this.options = new PhaseOptions();
        this.incomingAdapters = new ArrayList<PhaseAdapter>();
        this.outgoingAdapters = new ArrayList<PhaseAdapter>();
        this.inputMapping = new HashMap<Integer, TeamRepresentation>();
        this.outputMapping = new HashMap<Integer, TeamRepresentation>();
        this.name = name;
        this.rounds = new ArrayList<Round>();

        this.createPositionTitles();
    }

    /**
     * Create the rounds and games from the given data
     */
    public abstract void finalizeCreation();

    /**
     * Trigger the process of evaluating played games to create a
     * standing/output mapping for the next phase - return the standing even if
     * not all games have been played yet. As soon as all games are played,
     * write standings to outputMapping
     */
    protected abstract Map<Integer, TeamRepresentation> updateStandings();

    public abstract int getNumRounds();

    public List<Game> getGames() {
        List<Game> games = new ArrayList<Game>();
        for (Round round : this.getRounds()) {
            games.addAll(round.getGames());
        }
        return games;
    }

    public void addIncomingAdapter(PhaseAdapter adapter) {
        this.incomingAdapters.add(adapter);
    }

    public void addOutgoingAdapter(PhaseAdapter adapter) {
        this.outgoingAdapters.add(adapter);
    }

    protected void createTeamInputMapping() {
        for (PhaseAdapter adapter : this.incomingAdapters) {
            this.inputMapping.putAll(adapter.getNextTeamMapping());
        }
        for (Entry<Integer, TeamRepresentation> entry : this.inputMapping.entrySet()) {
            entry.getValue().setSeed(entry.getKey());
        }
    }

    /**
     * Returns a team-representation mapped to the according standing at the
     * beginning of this Phase (i.e. seed)
     *
     * @param standing
     * @return
     */
    public TeamRepresentation getTeamBySeed(Integer standing) {
        return this.inputMapping.get(standing);
    }

    /**
     * Returns a team-representation mapped to the according standing at the end
     * of this Phase (i.e. result)
     *
     * @param standing
     * @return
     */
    public TeamRepresentation getTeamByResult(Integer standing) {
        return this.outputMapping.get(standing);
    }

    /**
     * Created pseudo team objects with a title for this output seed (e.g.
     * "3. Pool C")
     */
    private void createPositionTitles() {
        for (int i = 1; i <= this.numTeams; i++) {
            this.outputMapping.put(i, new TeamRepresentation(i + ". " + this.name));
            this.outputMapping.get(i).setSeed(i);
        }
    }

}
