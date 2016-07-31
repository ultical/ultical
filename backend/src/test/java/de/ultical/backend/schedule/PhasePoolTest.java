package de.ultical.backend.schedule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.ultical.backend.schedule.PhasePool.PoolStats;
import de.ultical.backend.schedule.PhasePool.PoolStatsComparator;

public class PhasePoolTest {

    private static TeamRepresentation[] teams;
    private static final String PHASE_NAME = "Test Phase";
    private static final String TEAM_NAME_PREFIX = "Test Team ";

    @Mock
    PhaseAdapter incomingAdapter;

    private PhasePool phasePool;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Map<Integer, TeamRepresentation> inputTeamMapping = new HashMap<Integer, TeamRepresentation>();
        teams = new TeamRepresentation[3];
        for (int i = 0; i <= 2; i++) {
            teams[i] = new TeamRepresentation(TEAM_NAME_PREFIX + (i + 1));
            inputTeamMapping.put(i + 1, teams[i]);
        }
        when(this.incomingAdapter.getNextTeamMapping()).thenReturn(inputTeamMapping);

        this.phasePool = new PhasePool(PHASE_NAME, 3);
        this.phasePool.addIncomingAdapter(this.incomingAdapter);
    }

    @Test
    public void testPoolStatsComparator() {
        List<PoolStats> poolStats = new ArrayList<PoolStats>();
        for (int i = 0; i < 4; i++) {
            PoolStats newPoolStats = this.phasePool.new PoolStats(new TeamRepresentation("Team " + (i + 1)));
            newPoolStats.setWinnerPoints(i + 2);
            poolStats.add(newPoolStats);
        }

        Collections.sort(poolStats, this.phasePool.new PoolStatsComparator(PoolStatsComparator.SORT_BY_WINNER_POINTS));

        assertEquals(poolStats.get(0).getTeamRep().getName(), "Team 4");
        assertEquals(poolStats.get(3).getTeamRep().getName(), "Team 1");
    }

    /*
     * Input: Teams called "Seed 1" to "Seed 3"
     */
    @Test
    public void testPhaseFinalization() {
        // create games
        this.phasePool.finalizeCreation();

        // assert that there are 3 rounds with 3 games in total as one team
        // always should have a bye
        assertEquals(this.phasePool.getRounds().size(), 3);
        assertEquals(this.phasePool.getGames().size(), 3);

        // assert that the last game is seed 1 vs seed 2
        Game game = this.phasePool.getGames().get(2);
        if (game.getHome().equals(teams[0])) {
            assertEquals(game.getAway(), teams[1]);
        } else if (game.getHome().equals(teams[1])) {
            assertEquals(game.getAway(), teams[0]);
        }

        assertEquals(this.phasePool.getOutputMapping().get(1).getName(), "1. " + PHASE_NAME);

        // TODO: set options POOL_SHOW_GAMES_WITH_BYE and check for games with
        // bye
    }

    @Test
    public void testUpdateStandings() {
        // create games
        this.phasePool.finalizeCreation();

        // set some game results
        List<Game> games = this.phasePool.getGames();
        // game 0 - Seed 2 vs Seed 3
        games.get(0).setFinalScoreHome(15);
        games.get(0).setFinalScoreAway(12);
        games.get(0).setOver(true);
        // game 1 - Seed 3 vs Seed 1
        games.get(1).setFinalScoreHome(15);
        games.get(1).setFinalScoreAway(13);
        games.get(1).setOver(true);

        // check that no results are yet published in the outputMapping
        this.phasePool.updateStandings();
        assertEquals(this.phasePool.isComplete(), false);
        assertEquals(this.phasePool.getOutputMapping().get(1).getTitle(), "1. " + PHASE_NAME);

        // set missing game results
        // game 2 - Seed 2 vs Seed 1
        games.get(2).setFinalScoreHome(14);
        games.get(2).setFinalScoreAway(15);
        games.get(2).setOver(true);

        // check order in the results
        this.phasePool.updateStandings();
        assertEquals(this.phasePool.isComplete(), true);

        assertEquals(this.phasePool.getOutputMapping().get(1).getName(), TEAM_NAME_PREFIX + "2");
        assertEquals(this.phasePool.getOutputMapping().get(2).getName(), TEAM_NAME_PREFIX + "1");
        assertEquals(this.phasePool.getOutputMapping().get(3).getName(), TEAM_NAME_PREFIX + "3");
    }
}
