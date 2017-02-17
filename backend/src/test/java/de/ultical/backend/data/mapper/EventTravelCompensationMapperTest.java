package de.ultical.backend.data.mapper;

import java.time.LocalDate;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import de.ultical.backend.model.Contact;
import de.ultical.backend.model.DivisionAge;
import de.ultical.backend.model.DivisionRegistrationTeams;
import de.ultical.backend.model.DivisionType;
import de.ultical.backend.model.Event;
import de.ultical.backend.model.EventTravelCompensation;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.Season;
import de.ultical.backend.model.Surface;
import de.ultical.backend.model.Team;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.TournamentFormat;
import de.ultical.backend.utils.test.PrepareDBRule;

public class EventTravelCompensationMapperTest {

    @ClassRule
    public static PrepareDBRule RULE = new PrepareDBRule();

    private EventTravelCompensation etc;
    private EventTravelCompensationMapper mapper;

    @Before
    public void setUp() {
        this.etc = new EventTravelCompensation();
        TournamentFormat tf = new TournamentFormat();
        tf.setName("fubar");
        RULE.getSession().getMapper(tf.getMapper()).insert(tf);

        Season season = new Season();
        season.setSurface(Surface.TURF);
        season.setYear(2017);
        RULE.getSession().getMapper(season.getMapper()).insert(season);

        Contact con = new Contact();
        con.setEmail("fff");
        con.setName("Foo Bar");
        con.setPhone("123 4565");
        RULE.getSession().getMapper(con.getMapper()).insert(con);

        TournamentEdition te = new TournamentEdition();
        te.setTournamentFormat(tf);
        te.setSeason(season);
        te.setOrganizer(con);
        RULE.getSession().getMapper(te.getMapper()).insert(te);
        DivisionRegistrationTeams drt = new DivisionRegistrationTeams();
        drt.setDivisionAge(DivisionAge.REGULAR);
        drt.setDivisionType(DivisionType.OPEN);
        drt.setDivisionIdentifier("Test Division");
        RULE.getSession().getMapper(drt.getMapper()).insert(drt, te, false);

        Event event = new Event();
        event.setTournamentEdition(te);
        event.setStartDate(LocalDate.of(2017, 5, 20));
        event.setEndDate(LocalDate.of(2017, 5, 21));
        RULE.getSession().getMapper(event.getMapper()).insert(event);

        Team team = new Team();
        team.setName("Testteam");
        RULE.getSession().getMapper(team.getMapper()).insert(team);

        Roster roster = new Roster();
        roster.setSeason(season);
        roster.setTeam(team);
        roster.setDivisionAge(DivisionAge.REGULAR);
        roster.setDivisionType(DivisionType.OPEN);
        roster.setNameAddition("");
        RULE.getSession().getMapper(roster.getMapper()).insert(roster);

        this.etc.setRoster(roster);
        this.etc.setEvent(event);
        this.etc.setDivisionRegistration(drt);

        this.mapper = RULE.getSession().getMapper(this.etc.getMapper());
    }

    @Test
    public void test() throws Exception {
        this.etc.setDistance(10);
        this.etc.setFee(0f);
        this.etc.setPaid(Boolean.FALSE);

        this.mapper.insert(this.etc);
        final Integer etcId = this.etc.getId();

        Assert.assertTrue(this.etc.getId() > 0);
        Assert.assertEquals(0, this.etc.getVersion());

        this.etc.setDistance(11);
        final int updateCount = this.mapper.update(this.etc);
        Assert.assertEquals(1, updateCount);

        EventTravelCompensation readEtc = this.mapper.get(etcId);
        Assert.assertEquals(1, readEtc.getVersion());
        Assert.assertNotNull(readEtc.getRoster());
        Assert.assertNotNull(readEtc.getDivisionRegistration());
        Assert.assertNotNull(readEtc.getEvent());

        Assert.assertEquals(Integer.valueOf(0), this.mapper.update(this.etc));

        List<EventTravelCompensation> all = this.mapper.getAll();
        Assert.assertNotNull(all);
        Assert.assertEquals(1, all.size());

        this.mapper.delete(this.etc);
        Assert.assertNull(this.mapper.get(etcId));
    }
}
