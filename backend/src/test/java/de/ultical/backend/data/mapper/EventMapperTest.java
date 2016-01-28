package de.ultical.backend.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.exceptions.PersistenceException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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
import de.ultical.backend.model.TournamentFormat;
import de.ultical.backend.utils.test.PrepareDBRule;

public class EventMapperTest {

    @ClassRule
    public static PrepareDBRule DBRULE = new PrepareDBRule();
    private Event event;
    private static Location location;
    private static TournamentEdition edition;
    private static Season season;
    private static TournamentFormat format;
    private static String info = "This is some information";

    @BeforeClass
    public static void beforeClass() throws Exception {
        location = new Location();
        location.setCity("foo");
        location.setStreet("bar");
        location.setCountry("barcamp");
        location.setAdditionalInfo("blubb");
        DBRULE.getSession().getMapper(location.getMapper()).insert(location);

        season = new Season();
        season.setYear(2015);
        season.setSurface(Surface.TURF);
        DBRULE.getSession().getMapper(season.getMapper()).insert(season);

        format = new TournamentFormat();
        format.setName("test Format");
        format.setDescription("ipsum lori");
        DBRULE.getSession().getMapper(format.getMapper()).insert(format);

        edition = new TournamentEdition();
        edition.setTournamentFormat(format);
        edition.setSeason(season);
        edition.setRegistrationStart(LocalDate.of(2015, 1, 1));
        edition.setRegistrationEnd(LocalDate.of(2015, 5, 31));
        Contact org1 = new Contact();
        org1.setName("Orgi");
        DBRULE.getSession().getMapper(ContactMapper.class).insert(org1);
        edition.setOrganizer(org1);

        DBRULE.getSession().getMapper(TournamentEditionMapper.class).insert(edition);
        DBRULE.getSession().commit();
        DBRULE.closeSession();
    }

    @Before
    public void setUp() throws Exception {
        this.event = new Event();
        List<Location> locations = new ArrayList<Location>();
        locations.add(location);
        this.event.setLocations(locations);
        this.event.setEndDate(LocalDate.of(2015, 12, 6));
        this.event.setStartDate(LocalDate.of(2015, 12, 5));
        this.event.setInfo(info);

        Fee fee = new Fee();
        fee.setAmount(14.14);
        fee.setCurrency("EUR");
        fee.setType(FeeType.LUNCH);
        this.event.setFees(Collections.singletonList(fee));

        Contact contact = new Contact();
        contact.setEmail("abc@mail.com");
        contact.setName("Peter GmbH");
        contact.setPhone("0142353535");
        DBRULE.getSession().getMapper(ContactMapper.class).insert(contact);

        TournamentEdition te = new TournamentEdition();
        te.setHashtag("#udm16");
        te.setOrganizer(contact);
        te.setSeason(season);
        te.setTournamentFormat(format);
        te.setRegistrationStart(LocalDate.of(2015, 11, 1));
        te.setRegistrationEnd(LocalDate.of(2015, 11, 30));
        DBRULE.getSession().getMapper(TournamentEditionMapper.class).insert(te);

        this.event.setTournamentEdition(te);
        this.event.setLocalOrganizer(contact);
        this.event.setMatchdayNumber(2);
        this.event.setTournamentEdition(edition);

    }

    @Test
    public void test() {

        EventMapper mapper = DBRULE.getSession().getMapper(this.event.getMapper());

        assertEquals(0, this.event.getId());
        mapper.insert(this.event);
        Assert.assertThat(0, CoreMatchers.not(CoreMatchers.equalTo(this.event.getId())));
        final int eventId = this.event.getId();

        LocationMapper locMapper = DBRULE.getSession().getMapper(location.getMapper());
        locMapper.insert(location);
        locMapper.addToEvent(this.event.getId(), location.getId());

        Event readEvent = mapper.get(eventId);
        assertNotNull(readEvent);
        assertEquals(1, readEvent.getVersion());
        assertEquals(LocalDate.of(2015, 12, 6), readEvent.getEndDate());
        assertEquals(LocalDate.of(2015, 12, 5), readEvent.getStartDate());
        assertNotNull(readEvent.getFees());
        assertNotNull(readEvent.getLocalOrganizer());
        assertNotNull(readEvent.getLocations());
        assertEquals(1, readEvent.getLocations().size());
        assertEquals(info, readEvent.getInfo());
        assertNotNull(readEvent.getTournamentEdition());
        assertTrue(readEvent.getTournamentEdition() instanceof TournamentEdition);

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
        assertNotNull(readEvent.getLocations());
        assertEquals(info, readEvent.getInfo());
        assertEquals(1, readEvent.getLocations().size());
        assertNotNull(readEvent.getTournamentEdition());
        assertTrue(readEvent.getTournamentEdition() instanceof TournamentEdition);

        /*
         * we need a second edition as otherwise we will insert a second event
         * for a TournamentEditionSingle
         */
        TournamentEdition secondEditino = DBRULE.getSession().getMapper(TournamentEditionMapper.class).get(1);
        secondEditino.setRegistrationStart(LocalDate.now());
        secondEditino.setRegistrationEnd(LocalDate.now());
        secondEditino.setSeason(season);
        secondEditino.setOrganizer(readEvent.getLocalOrganizer());

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
    public void testTournamentEditionNull() {
        EventMapper mapper = DBRULE.getSession().getMapper(this.event.getMapper());
        this.event.setTournamentEdition(null);
        mapper.insert(this.event);
    }
}
