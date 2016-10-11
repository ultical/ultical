package de.ultical.backend.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import de.ultical.backend.model.Contact;
import de.ultical.backend.model.DivisionAge;
import de.ultical.backend.model.DivisionRegistration;
import de.ultical.backend.model.DivisionRegistration.DivisionRegistrationStatus;
import de.ultical.backend.model.DivisionRegistrationTeams;
import de.ultical.backend.model.DivisionType;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.Season;
import de.ultical.backend.model.Surface;
import de.ultical.backend.model.Team;
import de.ultical.backend.model.TeamRegistration;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.TournamentFormat;
import de.ultical.backend.utils.test.PrepareDBRule;

public class TeamRegistrationMapperTest {

    @ClassRule
    public static PrepareDBRule RULE = new PrepareDBRule();
    private TournamentEdition tes;
    private TeamRegistrationMapper mapper;
    private DivisionRegistrationTeams divisionRegOpen;
    private DivisionRegistrationTeams divisionRegMxd;
    private DivisionRegistrationMapper divRegMapper;
    private Team team;
    private Roster roster;
    private Season season;

    @Before
    public void setUp() throws Exception {
        TournamentFormat tf = new TournamentFormat();
        tf.setName("blb");
        tf.setDescription("something");
        RULE.getSession().getMapper(tf.getMapper()).insert(tf);

        this.season = new Season();
        this.season.setYear(2015);
        this.season.setSurface(Surface.TURF);
        RULE.getSession().getMapper(this.season.getMapper()).insert(this.season);

        this.tes = new TournamentEdition();
        this.tes.setName("alter");

        this.team = new Team();
        this.team.setName("Goldfingers");
        RULE.getSession().getMapper(this.team.getMapper()).insert(this.team);

        this.roster = new Roster();
        this.roster.setDivisionAge(DivisionAge.REGULAR);
        this.roster.setDivisionType(DivisionType.OPEN);
        this.roster.setTeam(this.team);
        this.roster.setSeason(this.season);
        this.roster.setNameAddition("");
        RULE.getSession().getMapper(this.roster.getMapper()).insert(this.roster);

        Contact contact = new Contact();
        contact.setEmail("abc@asd.de");
        contact.setName("Hans");
        this.tes.setOrganizer(contact);
        this.tes.setTournamentFormat(tf);
        this.tes.setSeason(this.season);
        this.tes.setRegistrationEnd(LocalDate.of(2015, 12, 6));
        this.tes.setRegistrationStart(LocalDate.of(2015, 12, 23));
        RULE.getSession().getMapper(ContactMapper.class).insert(contact);

        RULE.getSession().getMapper(this.tes.getMapper()).insert(this.tes);

        this.divisionRegOpen = new DivisionRegistrationTeams();
        this.divisionRegOpen.setDivisionAge(DivisionAge.REGULAR);
        this.divisionRegOpen.setDivisionType(DivisionType.OPEN);
        this.divisionRegOpen.setNumberSpots(12);
        this.divisionRegOpen.setDivisionIdentifier("Open");

        this.divisionRegMxd = new DivisionRegistrationTeams();
        this.divisionRegMxd.setDivisionAge(DivisionAge.REGULAR);
        this.divisionRegMxd.setDivisionType(DivisionType.MIXED);
        this.divisionRegMxd.setNumberSpots(12);
        this.divisionRegMxd.setDivisionIdentifier("Mixed");

        this.divRegMapper = RULE.getSession().getMapper(DivisionRegistrationMapper.class);

        this.mapper = RULE.getSession().getMapper(TeamRegistrationMapper.class);
    }

    public void tearDown() throws Exception {
        RULE.closeSession();
    }

    @Test
    public void test() {
        this.divRegMapper.insert(this.divisionRegOpen, this.tes, false);
        this.divRegMapper.insert(this.divisionRegMxd, this.tes, false);

        TournamentEdition foundEdition = RULE.getSession().getMapper(this.tes.getMapper()).get(this.tes.getId());
        assertNotNull(foundEdition);
        assertNotNull(foundEdition.getDivisionRegistrations());
        assertEquals(2, foundEdition.getDivisionRegistrations().size());

        TeamRegistration goldfingersRegistration = new TeamRegistration();
        goldfingersRegistration.setRoster(this.roster);
        goldfingersRegistration.setComment("the GUC is coming!");
        goldfingersRegistration.setStatus(DivisionRegistrationStatus.WAITING_LIST);
        goldfingersRegistration.setTeamName("Goldfingers");

        this.mapper.insert(this.divisionRegOpen.getId(), goldfingersRegistration);

        Team gucMixed = new Team();
        gucMixed.setName("Goldfingers");

        Roster rosterMixed = new Roster();
        rosterMixed.setDivisionAge(DivisionAge.REGULAR);
        rosterMixed.setDivisionType(DivisionType.MIXED);
        rosterMixed.setTeam(gucMixed);
        rosterMixed.setSeason(this.season);
        rosterMixed.setNameAddition("");

        TeamRegistration gucMixedReg = new TeamRegistration();
        gucMixedReg.setRoster(rosterMixed);
        gucMixedReg.setStatus(DivisionRegistrationStatus.PENDING);
        gucMixedReg.setTeamName("Goldfingers");
        RULE.getSession().getMapper(gucMixed.getMapper()).insert(gucMixed);
        RULE.getSession().getMapper(rosterMixed.getMapper()).insert(rosterMixed);

        this.mapper.insert(this.divisionRegMxd.getId(), gucMixedReg);

        Team wallCity = new Team();
        wallCity.setName("WallCity");
        RULE.getSession().getMapper(wallCity.getMapper()).insert(wallCity);

        Roster rosterWallCity = new Roster();
        rosterWallCity.setDivisionAge(DivisionAge.REGULAR);
        rosterWallCity.setDivisionType(DivisionType.OPEN);
        rosterWallCity.setTeam(wallCity);
        rosterWallCity.setNameAddition("");
        rosterWallCity.setSeason(this.season);
        RULE.getSession().getMapper(rosterWallCity.getMapper()).insert(rosterWallCity);

        TeamRegistration wallCityReg = new TeamRegistration();
        wallCityReg.setComment("Down comes the hammer!");
        wallCityReg.setRoster(rosterWallCity);
        wallCityReg.setStatus(DivisionRegistrationStatus.CONFIRMED);
        wallCityReg.setTeamName("WallCity");
        this.mapper.insert(this.divisionRegOpen.getId(), wallCityReg);

        foundEdition = RULE.getSession().getMapper(this.tes.getMapper()).get(this.tes.getId());
        assertNotNull(foundEdition);
        assertNotNull(foundEdition.getDivisionRegistrations());
        assertEquals(2, foundEdition.getDivisionRegistrations().size());

        for (DivisionRegistration divReg : foundEdition.getDivisionRegistrations()) {
            if (divReg.getDivisionType() == DivisionType.OPEN) {
                /*
                 * we registered guc and wallcity. GUC has been registerd first,
                 * thus it should be the first team in the list of
                 * registrations.
                 */
                DivisionRegistrationTeams openDivisionReg = (DivisionRegistrationTeams) divReg;
                Roster guc = openDivisionReg.getRegisteredTeams().get(0).getRoster();
                Roster wc = openDivisionReg.getRegisteredTeams().get(1).getRoster();
                assertEquals("Goldfingers", guc.getTeam().getName());
                assertEquals("WallCity", wc.getTeam().getName());
            }
        }
    }

}
