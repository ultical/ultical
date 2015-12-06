package de.ultical.backend.data.mapper;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.*;

import de.ultical.backend.model.*;
import de.ultical.backend.utils.test.PrepareDBRule;

public class TournamentEditionMapperTest {

	private static final LocalDate END_DATE = LocalDate.of(2015, 11, 1);
	private static final LocalDate START_DATE = LocalDate.of(2015, 10, 1);
	private static final String ORGANIZER_PHONE = "0171/1234567";
	private static final String ORGANIZER_EMAIL = "foo@bar.de";
	private static final String ORGANIZER_NAME = "Foo";
	private static final String ALTERNATIVE_NAME = "mein Test";
	TournamentEdition edition;
	TournamentEditionMapper mapper;
	
	@ClassRule
	public static PrepareDBRule DBRULE = new PrepareDBRule();
	
	@Before
	public void setUp() throws Exception {
		edition = new TournamentEditionSingle();
		
		TournamentFormat format = new TournamentFormat();
		format.setName("test format");
		format.setDescription("");
		DBRULE.getSession().getMapper(format.getMapper()).insert(format);
		Season season = new Season();
		season.setSurface(Surface.TURF);
		season.setYear(2015);
		DBRULE.getSession().getMapper(season.getMapper()).insert(season);
		
		edition.setTournamentFormat(format);
		edition.setAlternativeName(ALTERNATIVE_NAME);
		edition.setFeePerGuest(1);
		edition.setFeePerPlayer(2);
		edition.setFeePerTeam(3);
		edition.setSeason(season);
		edition.setOrganizerName(ORGANIZER_NAME);
		edition.setOrganizerEmail(ORGANIZER_EMAIL);
		edition.setOrganizerPhone(ORGANIZER_PHONE);
		edition.setRegistrationStart(START_DATE);
		edition.setRegistrationEnd(END_DATE);
		
		mapper = (TournamentEditionMapper) DBRULE.getSession().getMapper(edition.getMapper());
	}

	@Test
	public void test() {
		assertEquals(0, edition.getId());
		mapper.insert(edition);
		assertEquals(1,edition.getId());
		TournamentEdition readEdition = mapper.get(1);
		assertNotNull(readEdition);
		assertTrue(readEdition instanceof TournamentEditionSingle);
		assertNotNull(readEdition.getTournamentFormat());
		assertNotNull(readEdition.getSeason());
		assertEquals(ALTERNATIVE_NAME, readEdition.getAlternativeName());
		assertEquals(1, readEdition.getFeePerGuest());
		assertEquals(2, readEdition.getFeePerPlayer());
		assertEquals(3, readEdition.getFeePerTeam());
		assertEquals(ORGANIZER_NAME, readEdition.getOrganizerName());
		assertEquals(ORGANIZER_EMAIL, readEdition.getOrganizerEmail());
		assertEquals(ORGANIZER_PHONE, readEdition.getOrganizerPhone());
		assertEquals("EUR", readEdition.getCurrency());
		assertEquals(1, readEdition.getVersion());
		
		int updateCount = mapper.update(edition);
		assertEquals(0, updateCount);
		
		readEdition.setFeePerGuest(100);
		readEdition.setCurrency("GBP");
		updateCount = mapper.update(readEdition);
		assertEquals(1, updateCount);
		readEdition = mapper.get(edition.getId());
		assertNotNull(readEdition);
		assertTrue(readEdition instanceof TournamentEditionSingle);
		assertNotNull(readEdition.getTournamentFormat());
		assertNotNull(readEdition.getSeason());
		assertEquals(ALTERNATIVE_NAME, readEdition.getAlternativeName());
		assertEquals(100, readEdition.getFeePerGuest());
		assertEquals(2, readEdition.getFeePerPlayer());
		assertEquals(3, readEdition.getFeePerTeam());
		assertEquals(ORGANIZER_NAME, readEdition.getOrganizerName());
		assertEquals(ORGANIZER_EMAIL, readEdition.getOrganizerEmail());
		assertEquals(ORGANIZER_PHONE, readEdition.getOrganizerPhone());
		assertEquals("GBP", readEdition.getCurrency());
		
		mapper.insert(edition);
		assertNotEquals(edition.getId(), readEdition.getId());
		List<TournamentEdition> allEditions = mapper.getAll();
		assertNotNull(allEditions);
		assertEquals(2, allEditions.size());
		
		mapper.delete(readEdition);
		allEditions = mapper.getAll();
		assertNotNull(allEditions);
		assertEquals(1, allEditions.size());
		
	}

}
