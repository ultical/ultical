package de.ultical.backend.schedule;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import de.ultical.backend.model.Team;

public class ScheduleTest {

    public static final String TEAM_PREFIX = "TEAM ";

    private Schedule schedule;

    private PhasePool poolA, poolB;
    private PhaseBracket bracket;

    private PhaseAdapter toPoolA, toPoolB, toBracket1, toBracket2, toStandings;

    @Before
    public void setUp() {
        this.schedule = new Schedule(7);

        this.poolA = new PhasePool("Pool A", 4);
        this.schedule.addPhase(this.poolA);

        this.toPoolA = new PhaseAdapter();
        this.toPoolA.setPreviousPhase(this.schedule.getSeedingPhase());
        this.toPoolA.setNextPhase(this.poolA);
        this.toPoolA.addMapping(1, 1);
        this.toPoolA.addMapping(3, 2);
        this.toPoolA.addMapping(5, 3);
        this.toPoolA.addMapping(7, 4);

        this.poolB = new PhasePool("Pool B", 3);
        this.schedule.addPhase(this.poolB);

        this.toPoolB = new PhaseAdapter();
        this.toPoolB.setPreviousPhase(this.schedule.getSeedingPhase());
        this.toPoolB.setNextPhase(this.poolB);
        this.toPoolB.addMapping(2, 1);
        this.toPoolB.addMapping(4, 2);
        this.toPoolB.addMapping(6, 3);

        // bracket
        this.bracket = new PhaseBracket("Bracket", 7);
        this.schedule.addPhase(this.bracket);

        this.toBracket1 = new PhaseAdapter();
        this.toBracket1.setPreviousPhase(this.poolA);
        this.toBracket1.setNextPhase(this.bracket);
        this.toBracket1.addMapping(1, 1);
        this.toBracket1.addMapping(2, 3);
        this.toBracket1.addMapping(3, 5);
        this.toBracket1.addMapping(4, 7);

        this.toBracket2 = new PhaseAdapter();
        this.toBracket2.setPreviousPhase(this.poolB);
        this.toBracket2.setNextPhase(this.bracket);
        this.toBracket2.addMapping(1, 2);
        this.toBracket2.addMapping(2, 4);
        this.toBracket2.addMapping(3, 6);

        this.toStandings = new PhaseAdapter();
        this.toStandings.setPreviousPhase(this.bracket);
        this.toStandings.setNextPhase(this.schedule.getStandingPhase());
        this.toStandings.addMapping(1, 1);
        this.toStandings.addMapping(2, 2);
        this.toStandings.addMapping(3, 3);
        this.toStandings.addMapping(4, 4);
        this.toStandings.addMapping(5, 5);
        this.toStandings.addMapping(6, 6);
        this.toStandings.addMapping(7, 7);
    }

    @Test
    public void testGetRounds() {
        this.schedule.finalizeScheduleCreation();

        List<Round> rounds = this.schedule.getRounds();

        assertEquals(rounds.size(), 9);
        assertEquals(rounds.get(5).getPhaseTimingIndex(), 10002);
    }

    @Test
    public void testRun() {

        this.schedule.finalizeScheduleCreation();
        List<Round> rounds = this.schedule.getRounds();
        for (Round round : rounds) {
            for (Game game : round.getGames()) {
                game.setFinalScoreHome(15);
                game.setFinalScoreAway(13);
                game.setOver(true);
            }
        }

        for (int i = 1; i <= 8; i++) {
            Team team = new Team();
            team.setName(TEAM_PREFIX + i);
            this.schedule.setSeed(i, team);
        }
        this.schedule.updateSchedule();

        for (Round round : rounds) {
            System.out.println(round.getTitle() + " " + round.getTimingIndex());
            for (Game game : round.getGames()) {
                System.out.println(game);
            }
        }

        Map<Integer, Team> standings = this.schedule.getStandings();

        assertEquals(standings.get(1).getName(), TEAM_PREFIX + 5);
        assertEquals(standings.get(3).getName(), TEAM_PREFIX + 6);
        assertEquals(standings.get(6).getName(), TEAM_PREFIX + 1);
        assertEquals(standings.get(7).getName(), TEAM_PREFIX + 4);
    }
}
