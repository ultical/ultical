package de.ultical.backend.data.mapper;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import de.ultical.backend.model.Association;
import de.ultical.backend.model.Club;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.DivisionAge;
import de.ultical.backend.model.DivisionType;
import de.ultical.backend.model.Gender;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.Season;
import de.ultical.backend.model.Surface;
import de.ultical.backend.model.Team;
import de.ultical.backend.utils.test.PrepareDBRule;

public class RosterMapperTest {

    @ClassRule
    public static PrepareDBRule DBRULE = new PrepareDBRule();

    private static Team team;

    private static Season season;

    private static RosterMapper rosterMapper;

    private static DfvPlayer player1;

    private static DfvPlayer player2;

    private Roster roster;

    @BeforeClass
    public static void before() throws Exception {
        Association assoc = new Association();
        assoc.setAcronym("assoc");
        assoc.setName("Association");
        DBRULE.getSession().getMapper(assoc.getMapper()).insert(assoc);
        Club club = new Club();
        club.setName("test-club");
        club.setAssociation(assoc);
        ClubMapper cm = DBRULE.getSession().getMapper(club.getMapper());
        cm.insert(club);
        team = new Team();
        team.setClub(club);
        team.setDescription("Test Team");
        team.setName("Test Team");
        DBRULE.getSession().getMapper(team.getMapper()).insert(team);

        player1 = new DfvPlayer();
        player1.setFirstName("Player 1");
        player1.setLastName("lastName");
        player1.setDfvNumber(12345);
        player1.setGender(Gender.MALE);
        player1.setBirthDate(LocalDate.of(1981, 2, 4));
        player1.setLastModified(LocalDateTime.now());
        player1.setClub(club);

        final DfvPlayerMapper dpm = DBRULE.getSession().getMapper(player1.getMapper());
        final PlayerMapper pm = DBRULE.getSession().getMapper(PlayerMapper.class);
        pm.insertPlayer(player1, true);
        dpm.insert(player1);

        player2 = new DfvPlayer();
        player2.setFirstName("Player 2");
        player2.setLastName("lastName");
        player2.setDfvNumber(23456);
        player2.setGender(Gender.NA);
        player2.setBirthDate(LocalDate.of(1975, 11, 11));
        player2.setLastModified(LocalDateTime.now());
        player2.setClub(club);

        pm.insertPlayer(player2, true);
        dpm.insert(player2);

        season = new Season();
        season.setSurface(Surface.TURF);
        season.setYear(2016);
        season.setPlusOneYear(false);
        DBRULE.getSession().getMapper(season.getMapper()).insert(season);

        rosterMapper = DBRULE.getSession().getMapper(RosterMapper.class);
    }

    @Before
    public void setUp() throws Exception {
        this.roster = new Roster();
        this.roster.setSeason(season);
        this.roster.setTeam(team);
        this.roster.setDivisionAge(DivisionAge.REGULAR);
        this.roster.setDivisionType(DivisionType.OPEN);
        this.roster.setNameAddition("test1");
    }

    @After
    public void tearDown() throws Exception {
        if (this.roster != null) {
            rosterMapper.delete(this.roster.getId());
            this.roster = null;
        }
    }

    @Test
    public void testSafeRoster() throws Exception {
        rosterMapper.insert(this.roster);
        final Integer rosterId = this.roster.getId();
        assertThat(rosterId, notNullValue());
    }

    @Test
    public void testGetRoster() throws Exception {
        rosterMapper.insert(this.roster);
        final Integer rosterId = this.roster.getId();
        Roster readRoster = rosterMapper.get(rosterId);
        assertThat(readRoster, notNullValue());
        assertThat(readRoster.getId(), equalTo(rosterId));
        assertThat(readRoster.getSeason(), notNullValue());
        assertThat(readRoster.getSeason().getId(), equalTo(season.getId()));
        assertThat(readRoster.getDivisionAge(), equalTo(DivisionAge.REGULAR));
        assertThat(readRoster.getDivisionType(), equalTo(DivisionType.OPEN));
        assertThat(readRoster.getTeam(), notNullValue());
        assertThat(readRoster.getTeam().getId(), equalTo(team.getId()));
        assertThat(readRoster.getNameAddition(), equalTo("test1"));
        assertThat(readRoster.getVersion(), equalTo(1));
    }

    @Test
    public void testUpdateRoster() throws Exception {
        rosterMapper.insert(this.roster);
        final Integer rosterId = this.roster.getId();
        Roster readRoster = rosterMapper.get(rosterId);
        readRoster.setNameAddition("update");
        Integer updateCount = rosterMapper.update(readRoster);
        assertThat(updateCount, equalTo(1));

        readRoster = rosterMapper.get(rosterId);
        assertThat(readRoster, notNullValue());
        assertThat(readRoster.getId(), equalTo(rosterId));
        assertThat(readRoster.getVersion(), equalTo(2));
        assertThat(readRoster.getNameAddition(), equalTo("update"));
        assertThat(readRoster.getSeason(), notNullValue());
        assertThat(readRoster.getSeason().getId(), equalTo(season.getId()));
        assertThat(readRoster.getDivisionAge(), equalTo(DivisionAge.REGULAR));
        assertThat(readRoster.getDivisionType(), equalTo(DivisionType.OPEN));
        assertThat(readRoster.getTeam(), notNullValue());
        assertThat(readRoster.getTeam().getId(), equalTo(team.getId()));
    }

    @Test
    public void testGetRosterForPlayer() throws Exception {

        rosterMapper.insert(this.roster);

        rosterMapper.addPlayer(this.roster, player1);

        List<Roster> rostersPlayer1 = rosterMapper.getRostersForPlayer(player1);
        assertNotNull("we didn't expect null", rostersPlayer1);
        assertThat("roster's size doesn't match", rostersPlayer1.size(), equalTo(1));
        assertThat("we received the wrong roster", rostersPlayer1.get(0).getId(), equalTo(this.roster.getId()));

        List<Roster> rostersPlayer2 = rosterMapper.getRostersForPlayer(player2);
        assertNotNull("we didn't expect null", rostersPlayer2);
        assertThat("roster's size doesn't match", rostersPlayer2.isEmpty(), is(true));

        /*
         * add a second roster for player1
         */
        rosterMapper.insert(this.roster);
        rosterMapper.addPlayer(this.roster, player1);
        rostersPlayer1 = rosterMapper.getRostersForPlayer(player1);
        assertNotNull("we didn't expect null", rostersPlayer1);
        assertThat("roster's size doesn't match", rostersPlayer1.size(), equalTo(2));
    }

}
