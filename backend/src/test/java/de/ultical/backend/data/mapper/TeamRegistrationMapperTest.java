package de.ultical.backend.data.mapper;

import static org.junit.Assert.*;

import java.time.LocalDate;

import org.junit.*;

import de.ultical.backend.model.*;
import de.ultical.backend.utils.test.PrepareDBRule;

public class TeamRegistrationMapperTest {

	@ClassRule
	public static PrepareDBRule RULE = new PrepareDBRule();
	private TournamentEditionSingle tes;
	private TeamRegistrationMapper mapper;
	private TeamRegistration reg;
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
		
		tes = new TournamentEditionSingle();
		tes.setAlternativeName("alter");
		tes.setOrganizerName("dfdfd");
		tes.setOrganizerEmail("jkjk");
		tes.setTournamentFormat(tf);
		tes.setSeason(season);
		tes.setRegistrationEnd(LocalDate.of(2015, 12, 6));
		tes.setRegistrationStart(LocalDate.of(2015, 12, 23));
		RULE.getSession().getMapper(tes.getMapper()).insert(tes);
		
		team = new Team();
		team.setName("Goldfingers");
		RULE.getSession().getMapper(team.getMapper()).insert(team);
		
		divisionRegOpen = new DivisionRegistrationTeams();
		divisionRegOpen.setDivisionAge(DivisionAge.REGULAR);
		divisionRegOpen.setDivisionType(DivisionType.OPEN);
		divisionRegOpen.setNumberSpots(12);
		
		divisionRegMxd = new DivisionRegistrationTeams();
		divisionRegMxd.setDivisionAge(DivisionAge.REGULAR);
		divisionRegMxd.setDivisionType(DivisionType.MIXED);
		divisionRegMxd.setNumberSpots(12);
		
		divRegMapper = RULE.getSession().getMapper(DivisionRegistrationMapper.class);
		
		reg = new TeamRegistration();
		
		mapper = RULE.getSession().getMapper(TeamRegistrationMapper.class);
	}

	public void tearDown() throws Exception {
		RULE.closeSession();
	}
	@Test
	public void test() {
		divRegMapper.insert(divisionRegOpen, tes);
		divRegMapper.insert(divisionRegMxd,tes);
		
		TournamentEdition foundEdition = RULE.getSession().getMapper(tes.getMapper()).get(tes.getId());
		assertNotNull(foundEdition);
		assertNotNull(foundEdition.getDivisionRegistrations());
		assertEquals(2, foundEdition.getDivisionRegistrations().size());
		
		mapper.insertAtEnd(divisionRegOpen, team);
		Team gucMixed = new Team();
		gucMixed.setName("Goldfingers");
		RULE.getSession().getMapper(gucMixed.getMapper()).insert(gucMixed);
		mapper.insertAtEnd(divisionRegMxd, gucMixed);
		
		Team wallCity = new Team();
		wallCity.setName("WallCity");
		RULE.getSession().getMapper(wallCity.getMapper()).insert(wallCity);
		mapper.insertAtEnd(divisionRegOpen, wallCity);
		
		foundEdition = RULE.getSession().getMapper(tes.getMapper()).get(tes.getId());
		assertNotNull(foundEdition);
		assertNotNull(foundEdition.getDivisionRegistrations());
		assertEquals(2, foundEdition.getDivisionRegistrations().size());
		
		for (DivisionRegistration divReg : foundEdition.getDivisionRegistrations()) {
			if (divReg.getDivisionType() == DivisionType.OPEN) {
				/*
				 * we registered guc and wallcity. GUC has been registerd first, thus it should be the first team in the list of registrations.
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
