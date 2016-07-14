package de.ultical.backend.schedule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PhaseBracketTest {

    private static TeamRepresentation[] teamReps;
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
        teamReps = new TeamRepresentation[numTeams];
        for (int i = 0; i < numTeams; i++) {
            teamReps[i] = new TeamRepresentation(TEAM_NAME_PREFIX + (i + 1));
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

        assertEquals(this.phaseBracket.getGames().get(0).getTeam1().getName(), TEAM_NAME_PREFIX + "1");
        assertEquals(this.phaseBracket.getRounds().get(2).getGames().get(0).getTeam1().getName(), "Winner SF-1-1");
        assertEquals(this.phaseBracket.getRounds().get(2).getGames().get(3).getTeam2().getName(), "Looser SF-5-2");
    }

}
