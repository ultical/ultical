package de.ultical.backend.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.exceptions.PersistenceException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import de.ultical.backend.model.Contact;
import de.ultical.backend.model.Event;
import de.ultical.backend.model.Fee;
import de.ultical.backend.model.FeeType;
import de.ultical.backend.model.Location;
import de.ultical.backend.model.Season;
import de.ultical.backend.model.Surface;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.TournamentEditionSingle;
import de.ultical.backend.model.TournamentFormat;
import de.ultical.backend.utils.test.PrepareDBRule;

public class EventMapperTest {

    @ClassRule
    public static PrepareDBRule DBRULE = new PrepareDBRule();
    private Event event;
    private Location location;
    private TournamentEdition edition;

    @Before
    public void beforeClass() throws Exception {
        this.location = new Location();
        this.location.setCity("foo");
        this.location.setStreet("bar");
        this.location.setCountry("barcamp");
        this.location.setAdditionalInfo("blubb");
        DBRULE.getSession().getMapper(this.location.getMapper()).insert(this.location);

        Season season = new Season();
        season.setYear(2015);
        season.setSurface(Surface.TURF);
        DBRULE.getSession().getMapper(season.getMapper()).insert(season);

        TournamentFormat format = new TournamentFormat();
        format.setName("test Format");
        format.setDescription("ipsum lori");
        DBRULE.getSession().getMapper(format.getMapper()).insert(format);

        this.edition = new TournamentEditionSingle();
        this.edition.setTournamentFormat(format);
        this.edition.setSeason(season);
        this.edition.setRegistrationStart(LocalDate.of(2015, 1, 1));
        this.edition.setRegistrationEnd(LocalDate.of(2015, 5, 31));
        Contact org1 = new Contact();
        org1.setName("Orgi");
        DBRULE.getSession().getMapper(ContactMapper.class).insert(org1);
        this.edition.setOrganizer(org1);

        DBRULE.getSession().getMapper(TournamentEditionMapper.class).insert(this.edition);
        DBRULE.closeSession();
    }

    @Before
    public void setUp() throws Exception {
        this.event = new Event();
        this.event.setLocation(this.location);
        this.event.setEndDate(LocalDate.of(2015, 12, 6));
        this.event.setStartDate(LocalDate.of(2015, 12, 5));

        Fee fee = new Fee();
        fee.setAmount(14.14);
        fee.setCurrency("EUR");
        fee.setType(FeeType.LUNCH);
        this.event.setFees(Collections.singletonList(fee));

        Contact contact = new Contact();
        contact.setEmail("abc@mail.com");
        contact.setName("Peter GmbH");
        contact.setPhone("0142353535");

        this.event.setLocalOrganizer(contact);
        this.event.setMatchdayNumber(2);
        this.event.setTournamentEdition(this.edition);

    }

    @Test
    public void test() {

        EventMapper mapper = DBRULE.getSession().getMapper(this.event.getMapper());

        assertEquals(0, this.event.getId());
        mapper.insert(this.event);
        Assert.assertThat(0, CoreMatchers.not(CoreMatchers.equalTo(this.event.getId())));
        final int eventId = this.event.getId();

        Event readEvent = mapper.get(eventId);
        assertNotNull(readEvent);
        assertEquals(1, readEvent.getVersion());
        assertEquals(LocalDate.of(2015, 12, 6), readEvent.getEndDate());
        assertEquals(LocalDate.of(2015, 12, 5), readEvent.getStartDate());
        assertNotNull(readEvent.getFees());
        assertNotNull(readEvent.getLocalOrganizer());
        assertNotNull(readEvent.getLocation());
        assertNotNull(readEvent.getTournamentEdition());
        assertTrue(readEvent.getTournamentEdition() instanceof TournamentEditionSingle);

        int updateCount = mapper.update(this.event);
        assertEquals(0, updateCount);

        readEvent.setStartDate(LocalDate.of(2015, 12, 4));
        updateCount = mapper.update(readEvent);
        assertEquals(1, updateCount);

        readEvent = mapper.get(eventId);
        assertNotNull(readEvent);
        assertEquals(2, readEvent.getVersion());
        assertEquals(LocalDate.of(2015, 12, 6), readEvent.getEndDate());
        assertEquals(LocalDate.of(2015, 12, 4), readEvent.getStartDate());
        assertNotNull(readEvent.getFees());
        assertNotNull(readEvent.getLocalOrganizer());
        assertNotNull(readEvent.getLocation());
        assertNotNull(readEvent.getTournamentEdition());
        assertTrue(readEvent.getTournamentEdition() instanceof TournamentEditionSingle);

        /*
         * we need a second edition as otherwise we will insert a second event
         * for a TournamentEditionSingle
         */
        TournamentEdition secondEditino = DBRULE.getSession().getMapper(TournamentEditionMapper.class).get(1);
        DBRULE.getSession().getMapper(TournamentEditionMapper.class).insert(secondEditino);
        this.event.setTournamentEdition(secondEditino);

        mapper.insert(this.event);
        List<Event> allEvents = mapper.getAll();
        assertNotNull(allEvents);
        assertEquals(2, allEvents.size());

        final int deletedId = this.event.getId();
        mapper.delete(this.event);
        assertNull(mapper.get(deletedId));

        allEvents = mapper.getAll();
        assertNotNull(allEvents);
        assertEquals(1, allEvents.size());
    }

    @Test(expected = PersistenceException.class)
    public void testStartDateNull() {
        EventMapper mapper = DBRULE.getSession().getMapper(this.event.getMapper());
        this.event.setStartDate(null);
        mapper.insert(this.event);
    }

    @Test(expected = PersistenceException.class)
    public void testEndDateNull() {
        EventMapper mapper = DBRULE.getSession().getMapper(this.event.getMapper());
        this.event.setEndDate(null);
        mapper.insert(this.event);
    }

    @Test(expected = PersistenceException.class)
    public void testLocationNull() {
        EventMapper mapper = DBRULE.getSession().getMapper(this.event.getMapper());
        this.event.setLocation(null);
        mapper.insert(this.event);
    }

    @Test(expected = PersistenceException.class)
    public void testTournamentEditionNull() {
        EventMapper mapper = DBRULE.getSession().getMapper(this.event.getMapper());
        this.event.setTournamentEdition(null);
        mapper.insert(this.event);
    }
}
