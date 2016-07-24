package de.ultical.backend.schedule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.ultical.backend.model.Team;

public class PhaseBracketTest {

    private static final String PHASE_NAME = "Test Phase";
    private static final String TEAM_NAME_PREFIX = "Test Team ";

    @Mock
    PhaseAdapter incomingAdapter;

    private PhaseBracket phaseBracket;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        int numTeams = 7;

        Map<Integer, TeamRepresentation> inputTeamMapping = new HashMap<Integer, TeamRepresentation>();
        TeamRepresentation[] teamReps = new TeamRepresentation[numTeams];
        for (int i = 0; i < numTeams; i++) {
            Team team = new Team();
            team.setName(TEAM_NAME_PREFIX + (i + 1));
            teamReps[i] = new TeamRepresentation(team);
            inputTeamMapping.put(i + 1, teamReps[i]);
        }
        when(this.incomingAdapter.getNextTeamMapping()).thenReturn(inputTeamMapping);

        this.phaseBracket = new PhaseBracket(PHASE_NAME, numTeams);
        this.phaseBracket.addIncomingAdapter(this.incomingAdapter);
    }

    @Test
    public void testGetNumRounds() {
        this.phaseBracket = new PhaseBracket(PHASE_NAME, 4);
        assertEquals(this.phaseBracket.getNumRounds(), 2);
        this.phaseBracket = new PhaseBracket(PHASE_NAME, 5);
        assertEquals(this.phaseBracket.getNumRounds(), 3);
    }

    /*
     * Input: Teams called TEAM_NAME_PREFIX 1-x
     */
    @Test
    public void testFinalizeCreation() {
        this.phaseBracket.finalizeCreation();

        assertEquals(this.phaseBracket.getGames().get(0).getHome().getName(), TEAM_NAME_PREFIX + "1");
        assertEquals(this.phaseBracket.getRounds().get(2).getGames().get(0).getHome().getName(), "Winner SF-1-1");
        assertEquals(this.phaseBracket.getRounds().get(2).getGames().get(3).getAway().getName(), "Looser SF-5-2");
    }

    @Test
    public void testUpdateStanding() {
        this.phaseBracket.finalizeCreation();

        List<Game> games1 = this.phaseBracket.getRounds().get(0).getGames();

        games1.get(1).setFinalScoreHome(14);
        games1.get(1).setFinalScoreAway(15);
        games1.get(1).setOver(true);

        games1.get(2).setFinalScoreHome(13);
        games1.get(2).setFinalScoreAway(14);
        games1.get(2).setOver(true);

        games1.get(3).setFinalScoreHome(15);
        games1.get(3).setFinalScoreAway(12);
        games1.get(3).setOver(true);

        this.phaseBracket.updateStandings();

        assertEquals(this.phaseBracket.getRounds().get(1).getGames().get(1).getHome().getName(), TEAM_NAME_PREFIX + 7);
        assertEquals(this.phaseBracket.getRounds().get(1).getGames().get(1).getAway().getName(), TEAM_NAME_PREFIX + 3);
        assertEquals(this.phaseBracket.getRounds().get(1).getGames().get(2).getHome().isBye(), true);
        assertEquals(this.phaseBracket.getRounds().get(1).getGames().get(3).getAway().getName(), TEAM_NAME_PREFIX + 2);

        List<Game> games2 = this.phaseBracket.getRounds().get(1).getGames();

        games2.get(0).setFinalScoreHome(15);
        games2.get(0).setFinalScoreAway(14);
        games2.get(0).setOver(true);

        games2.get(1).setFinalScoreHome(1);
        games2.get(1).setFinalScoreAway(15);
        games2.get(1).setOver(true);

        games2.get(3).setFinalScoreHome(15);
        games2.get(3).setFinalScoreAway(13);
        games2.get(3).setOver(true);

        this.phaseBracket.updateStandings();

        List<Game> games3 = this.phaseBracket.getRounds().get(2).getGames();

        games3.get(0).setFinalScoreHome(15);
        games3.get(0).setFinalScoreAway(14);
        games3.get(0).setOver(true);

        games3.get(1).setFinalScoreHome(15);
        games3.get(1).setFinalScoreAway(14);
        games3.get(1).setOver(true);

        games3.get(2).setFinalScoreHome(15);
        games3.get(2).setFinalScoreAway(14);
        games3.get(2).setOver(true);

        this.phaseBracket.updateStandings();

        assertEquals(this.phaseBracket.getOutputMapping().get(1).getName(), TEAM_NAME_PREFIX + 1);
        assertEquals(this.phaseBracket.getOutputMapping().get(2).getName(), TEAM_NAME_PREFIX + 3);
        assertEquals(this.phaseBracket.getOutputMapping().get(3).getName(), TEAM_NAME_PREFIX + 5);
        assertEquals(this.phaseBracket.getOutputMapping().get(4).getName(), TEAM_NAME_PREFIX + 7);
        assertEquals(this.phaseBracket.getOutputMapping().get(5).getName(), TEAM_NAME_PREFIX + 4);
        assertEquals(this.phaseBracket.getOutputMapping().get(6).getName(), TEAM_NAME_PREFIX + 2);
        assertEquals(this.phaseBracket.getOutputMapping().get(7).getName(), TEAM_NAME_PREFIX + 6);
    }

}
