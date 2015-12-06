package de.ultical.backend.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.exceptions.PersistenceException;
import org.hamcrest.CoreMatchers;
import org.junit.*;

import de.ultical.backend.model.*;
import de.ultical.backend.utils.test.PrepareDBRule;

public class EventMapperTest {

	@ClassRule public static PrepareDBRule DBRULE = new PrepareDBRule();
	private Event event;
	private EventMapper mapper;
	
	@Before
	public void setUp() throws Exception {
		event = new Event();
		Location loc = new Location();
		loc.setCity("foo");
		loc.setStreet("bar");
		loc.setCountry("barcamp");
		loc.setAdditionalInfo("blubb");
		DBRULE.getSession().getMapper(loc.getMapper()).insert(loc);
		
		Season season = new Season();
		season.setYear(2015);
		season.setSurface(Surface.TURF);
		DBRULE.getSession().getMapper(season.getMapper()).insert(season);
		
		TournamentFormat format = new TournamentFormat();
		format.setName("test Format");
		format.setDescription("ipsum lori");
		DBRULE.getSession().getMapper(format.getMapper()).insert(format);
		
		TournamentEdition edition = new TournamentEditionSingle();
		edition.setTournamentFormat(format);
		edition.setSeason(season);
		edition.setRegistrationStart(LocalDate.of(2015,1,1));
		edition.setRegistrationEnd(LocalDate.of(2015, 5, 31));
		DBRULE.getSession().getMapper(TournamentEditionMapper.class).insert(edition);
		
		event.setLocation(loc);
		event.setEndDate(LocalDate.of(2015, 12, 6));
		event.setStartDate(LocalDate.of(2015, 12, 5));
		event.setFeePerBreakfast(1);
		event.setFeePerDinner(2);
		event.setFeePerGuest(3);
		event.setFeePerLunch(4);
		event.setFeePerNight(5);
		event.setFeePerPlayer(6);
		event.setFeePerTeam(7);
		event.setLocalOrganizerEmail("foo@bar.de");
		event.setLocalOrganizerName("FooBar");
		event.setLocalOrganizerPhone("0171/12345676");
		event.setMatchdayNumber(2);
		event.setTournamentEdition(edition);
		
		mapper = DBRULE.getSession().getMapper(event.getMapper());
	}

	@Test
	public void test() {
		assertEquals(0, event.getId());
		mapper.insert(event);
		Assert.assertThat(0, CoreMatchers.not(CoreMatchers.equalTo(event.getId())));
		final int eventId = event.getId();
		
		Event readEvent = mapper.get(eventId);
		assertNotNull(readEvent);
		assertEquals(1, readEvent.getVersion());
		assertEquals(LocalDate.of(2015, 12, 6), readEvent.getEndDate());
		assertEquals(LocalDate.of(2015, 12, 5),readEvent.getStartDate());
		assertEquals(1, readEvent.getFeePerBreakfast());
		assertEquals(2, readEvent.getFeePerDinner());
		assertEquals(3, readEvent.getFeePerGuest());
		assertEquals(4, readEvent.getFeePerLunch());
		assertEquals(5, readEvent.getFeePerNight());
		assertEquals(6, readEvent.getFeePerPlayer());
		assertEquals(7, readEvent.getFeePerTeam());
		assertEquals("foo@bar.de", readEvent.getLocalOrganizerEmail());
		assertEquals("FooBar", readEvent.getLocalOrganizerName());
		assertEquals("0171/12345676", readEvent.getLocalOrganizerPhone());
		assertNotNull(readEvent.getLocation());
		assertNotNull(readEvent.getTournamentEdition());
		assertTrue(readEvent.getTournamentEdition() instanceof TournamentEditionSingle);
		
		int updateCount = mapper.update(event);
		assertEquals(0, updateCount);
		
		readEvent.setLocalOrganizerName("BarFoo");
		updateCount = mapper.update(readEvent);
		assertEquals(1, updateCount);
		
		readEvent = mapper.get(eventId);
		assertNotNull(readEvent);
		assertEquals(2, readEvent.getVersion());
		assertEquals(LocalDate.of(2015, 12, 6), readEvent.getEndDate());
		assertEquals(LocalDate.of(2015, 12, 5),readEvent.getStartDate());
		assertEquals(1, readEvent.getFeePerBreakfast());
		assertEquals(2, readEvent.getFeePerDinner());
		assertEquals(3, readEvent.getFeePerGuest());
		assertEquals(4, readEvent.getFeePerLunch());
		assertEquals(5, readEvent.getFeePerNight());
		assertEquals(6, readEvent.getFeePerPlayer());
		assertEquals(7, readEvent.getFeePerTeam());
		assertEquals("foo@bar.de", readEvent.getLocalOrganizerEmail());
		assertEquals("BarFoo", readEvent.getLocalOrganizerName());
		assertEquals("0171/12345676", readEvent.getLocalOrganizerPhone());
		assertNotNull(readEvent.getLocation());
		assertNotNull(readEvent.getTournamentEdition());
		assertTrue(readEvent.getTournamentEdition() instanceof TournamentEditionSingle);
		
		/*
		 * we need a second edition as otherwise we will insert a second event for a TournamentEditionSingle
		 */
		TournamentEdition secondEditino = DBRULE.getSession().getMapper(TournamentEditionMapper.class).get(1);
		DBRULE.getSession().getMapper(TournamentEditionMapper.class).insert(secondEditino);
		event.setTournamentEdition(secondEditino);
		
		mapper.insert(event);
		List<Event> allEvents = mapper.getAll();
		assertNotNull(allEvents);
		assertEquals(2,allEvents.size());
		
		final int deletedId = event.getId();
		mapper.delete(event);
		assertNull(mapper.get(deletedId));
		
		allEvents = mapper.getAll();
		assertNotNull(allEvents);
		assertEquals(1,allEvents.size());
	}
	
	@Test(expected=PersistenceException.class)
	public void testStartDateNull() {
		event.setStartDate(null);
		mapper.insert(event);
	}
	
	@Test(expected=PersistenceException.class)
	public void testEndDateNull() {
		event.setEndDate(null);
		mapper.insert(event);
	}

	@Test(expected=PersistenceException.class)
	public void testLocationNull() {
		event.setLocation(null);
		mapper.insert(event);
	}
	
	@Test(expected=PersistenceException.class)
	public void testTournamentEditionNull() {
		event.setTournamentEdition(null);
		mapper.insert(event);
	}
}
