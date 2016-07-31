package de.ultical.backend.schedule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.ultical.backend.model.Team;

public class PhaseStandingsTest {

    private static TeamRepresentation[] teamReps;
    private static final String PHASE_NAME = "Test Phase";
    private static final String TEAM_NAME_PREFIX = "Test Team ";

    @Mock
    PhaseAdapter incomingAdapter;

    private PhaseStandings phaseStandings;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Map<Integer, TeamRepresentation> inputTeamMapping = new HashMap<Integer, TeamRepresentation>();
        teamReps = new TeamRepresentation[4];
        for (int i = 0; i <= 3; i++) {
            Team team = new Team();
            team.setName(TEAM_NAME_PREFIX + (i + 1));
            teamReps[i] = new TeamRepresentation(team);
            inputTeamMapping.put(i + 1, teamReps[i]);
        }
        when(this.incomingAdapter.getNextTeamMapping()).thenReturn(inputTeamMapping);

        this.phaseStandings = new PhaseStandings(PHASE_NAME, 4);
        this.phaseStandings.addIncomingAdapter(this.incomingAdapter);
    }

    /*
     * Input: Teams called TEAM_NAME_PREFIX 1-4
     */
    @Test
    public void testGetStandings() {

        Map<Integer, Team> standings = this.phaseStandings.getStandings();

        assertEquals(standings.get(1).getName(), TEAM_NAME_PREFIX + "1");
        assertEquals(standings.get(2).getName(), TEAM_NAME_PREFIX + "2");
        assertEquals(standings.get(3).getName(), TEAM_NAME_PREFIX + "3");
        assertEquals(standings.get(4).getName(), TEAM_NAME_PREFIX + "4");
    }

}
