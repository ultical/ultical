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
import de.ultical.backend.model.DivisionRegistrationTeams;
import de.ultical.backend.model.DivisionType;
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
    private Team team;
    private DivisionRegistrationTeams divisionRegOpen;
    private DivisionRegistrationTeams divisionRegMxd;
    private DivisionRegistrationMapper divRegMapper;

    @Before
    public void setUp() throws Exception {
        TournamentFormat tf = new TournamentFormat();
        tf.setName("blb");
        tf.setDescription("something");
        RULE.getSession().getMapper(tf.getMapper()).insert(tf);

        Season season = new Season();
        season.setYear(2015);
        season.setSurface(Surface.TURF);
        RULE.getSession().getMapper(season.getMapper()).insert(season);

        this.tes = new TournamentEdition();
        this.tes.setAlternativeName("alter");

        Contact contact = new Contact();
        contact.setEmail("abc@asd.de");
        contact.setName("Hans");
        this.tes.setOrganizer(contact);
        this.tes.setTournamentFormat(tf);
        this.tes.setSeason(season);
        this.tes.setRegistrationEnd(LocalDate.of(2015, 12, 6));
        this.tes.setRegistrationStart(LocalDate.of(2015, 12, 23));
        RULE.getSession().getMapper(ContactMapper.class).insert(contact);

        RULE.getSession().getMapper(this.tes.getMapper()).insert(this.tes);

        this.team = new Team();
        this.team.setName("Goldfingers");
        RULE.getSession().getMapper(this.team.getMapper()).insert(this.team);

        this.divisionRegOpen = new DivisionRegistrationTeams();
        this.divisionRegOpen.setDivisionAge(DivisionAge.REGULAR);
        this.divisionRegOpen.setDivisionType(DivisionType.OPEN);
        this.divisionRegOpen.setNumberSpots(12);

        this.divisionRegMxd = new DivisionRegistrationTeams();
        this.divisionRegMxd.setDivisionAge(DivisionAge.REGULAR);
        this.divisionRegMxd.setDivisionType(DivisionType.MIXED);
        this.divisionRegMxd.setNumberSpots(12);

        this.divRegMapper = RULE.getSession().getMapper(DivisionRegistrationMapper.class);

        this.mapper = RULE.getSession().getMapper(TeamRegistrationMapper.class);
    }

    public void tearDown() throws Exception {
        RULE.closeSession();
    }

    @Test
    public void test() {
        this.divRegMapper.insert(this.divisionRegOpen, this.tes);
        this.divRegMapper.insert(this.divisionRegMxd, this.tes);

        TournamentEdition foundEdition = RULE.getSession().getMapper(this.tes.getMapper()).get(this.tes.getId());
        assertNotNull(foundEdition);
        assertNotNull(foundEdition.getDivisionRegistrations());
        assertEquals(2, foundEdition.getDivisionRegistrations().size());

        TeamRegistration goldfingersRegistration = new TeamRegistration();
        goldfingersRegistration.setTeam(this.team);
        goldfingersRegistration.setComment("the GUC is coming!");
        this.mapper.insertAtEnd(this.divisionRegOpen, goldfingersRegistration);

        Team gucMixed = new Team();
        gucMixed.setName("Goldfingers");
        TeamRegistration gucMixedReg = new TeamRegistration();
        gucMixedReg.setTeam(gucMixed);
        RULE.getSession().getMapper(gucMixed.getMapper()).insert(gucMixed);
        this.mapper.insertAtEnd(this.divisionRegMxd, gucMixedReg);

        Team wallCity = new Team();
        wallCity.setName("WallCity");
        TeamRegistration wallCityReg = new TeamRegistration();
        wallCityReg.setComment("Down comes the hammer!");
        wallCityReg.setTeam(wallCity);
        RULE.getSession().getMapper(wallCity.getMapper()).insert(wallCity);
        this.mapper.insertAtEnd(this.divisionRegOpen, wallCityReg);

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
                Team guc = openDivisionReg.getRegisteredTeams().get(0).getTeam();
                Team wc = openDivisionReg.getRegisteredTeams().get(1).getTeam();
                assertEquals("Goldfingers", guc.getName());
                assertEquals("WallCity", wc.getName());
            }
        }
    }

}
