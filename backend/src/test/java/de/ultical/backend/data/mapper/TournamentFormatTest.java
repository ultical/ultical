package de.ultical.backend.data.mapper;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.*;

import de.ultical.backend.model.*;
import de.ultical.backend.utils.test.PrepareDBRule;

public class TournamentFormatTest {

	private static final String DESCRIPTION = "bestes Strandturnier ever";

	private static final String NAME = "Goldstrand";

	@ClassRule
	public static PrepareDBRule DBRULE = new PrepareDBRule();
	
	private TournamentFormat format;
	private TournamentFormatMapper mapper;

	private User admin;
	
	@Before
	public void setUp() throws Exception {
		format = new TournamentFormat();
		format.setName(NAME);
		format.setDescription(DESCRIPTION);
		
		final DfvPlayer player = new DfvPlayer();
		player.setFirstName("Brodie");
		player.setLastName("Smith");
		player.setGender(Gender.MALE);
		player.setDfvNumber("123456");
		player.setBirthDate(LocalDate.of(1979, 1, 25));
		DBRULE.getSession().getMapper(PlayerMapper.class).insert(player);
		DBRULE.getSession().getMapper(player.getMapper()).insert(player);
		
		admin = new User();
		admin.setDfvPlayer(player);
		admin.setEmail("q@q.de");
		admin.setPassword("secret");
		DBRULE.getSession().getMapper(admin.getMapper()).insert(admin);
		
		mapper = DBRULE.getSession().getMapper(format.getMapper());
	}

	@Test
	public void test() {
		assertEquals(0,format.getId());
		mapper.insert(format);
		DBRULE.getSession().commit();
		int formatId = format.getId();
		
		TournamentFormat readFormat = mapper.get(formatId);
		assertNotNull(readFormat);
		assertEquals(1, readFormat.getVersion());
		assertEquals(NAME, readFormat.getName());
		assertEquals(DESCRIPTION, readFormat.getDescription());
		
		this.format.setName("SOTB");
		int updCount = mapper.update(format);
		assertEquals(0, updCount);
		
		readFormat.setName("SOTB");
		updCount = mapper.update(readFormat);
		assertEquals(1, updCount);
		DBRULE.getSession().commit();
		
		readFormat = mapper.get(formatId);
		assertEquals("SOTB", readFormat.getName());
		assertEquals(DESCRIPTION, readFormat.getDescription());
		assertEquals(2, readFormat.getVersion());
		
		mapper.insert(format);
		DBRULE.getSession().commit();
		
		List<TournamentFormat> allFormats = mapper.getAll();
		assertNotNull(allFormats);
		assertEquals(2, allFormats.size());
		
		mapper.insertAdmin(format, admin);
		readFormat = mapper.get(format.getId());
		assertNotNull(readFormat);
		assertNotNull(readFormat.getAdmins());
		assertFalse(readFormat.getAdmins().isEmpty());
		assertEquals(1, readFormat.getAdmins().size());
		
		/*
		 * create a sample TournamentEdtion to check whether they were set correctly.
		 */
		TournamentEditionSingle tes = new TournamentEditionSingle();
		Season season = new Season();
		season.setYear(2015);
		season.setSurface(Surface.TURF);
		tes.setSeason(season);
		tes.setTournamentFormat(format);
		tes.setRegistrationStart(LocalDate.of(2015,10,1));
		tes.setRegistrationEnd(LocalDate.of(2015, 12, 21));
		tes.setOrganizerEmail("foo");
		tes.setOrganizerName("bar");
		
		/*
		 * after these three lines, the TournamentFormat format should have two editions.
		 */
		DBRULE.getSession().getMapper(SeasonMapper.class).insert(season);
		DBRULE.getSession().getMapper(TournamentEditionMapper.class).insert(tes);
		DBRULE.getSession().getMapper(TournamentEditionMapper.class).insert(tes);
		
		TournamentFormat formatWithEditions = mapper.get(format.getId());
		assertNotNull(formatWithEditions);
		assertNotNull(formatWithEditions.getEditions());
		assertEquals(2,formatWithEditions.getEditions().size());
		
		
		mapper.delete(format);
		DBRULE.getSession().commit();
		
		allFormats = mapper.getAll();
		assertNotNull(allFormats);
		assertEquals(1, allFormats.size());
		assertNull(mapper.get(2));
		
		
	}

}
