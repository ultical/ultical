package de.ultical.backend.api;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import javax.ws.rs.WebApplicationException;
import org.apache.ibatis.exceptions.PersistenceException;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Event;
import de.ultical.backend.model.User;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.TournamentFormat;
import de.ultical.backend.model.DivisionRegistration;

public class EventsResourceTest {

    private final static int KNOWN_EVENT_ID = 1;
    private final static int UNKNOWN_EVENT_ID = 2;
    private final static int EXCEPTION_EVENT_ID = 3;
    private final static int USER_ID = 42;
    private final static int UPDATEABLE_DIV_ID = 52;
    private final static int NON_UPDATEABLE_DIV_ID = 53;
    
    private EventsResource resource;
    
    @Mock
    private DataStore ds;
    @Mock
    private Event event1;
    @Mock
    private Event event2;
    @Mock
    private Event event3;
    @Mock
    private User user;
    @Mock
    private DivisionRegistration storedDiv;
    @Mock
    private DivisionRegistration updateDiv;
    @Mock
    private DivisionRegistration nonUpdateDiv;

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
	this.resource = new EventsResource();
	this.resource.dataStore = this.ds;
	when(this.ds.getAll(Event.class)).thenReturn(Arrays.asList(event1, event2, event3));
	when(this.ds.get(KNOWN_EVENT_ID, Event.class)).thenReturn(this.event1);
	when(this.ds.get(UNKNOWN_EVENT_ID, Event.class)).thenReturn(null);
	when(this.ds.get(EXCEPTION_EVENT_ID, Event.class)).thenThrow(new PersistenceException());
	when(this.ds.addNew(this.event1)).thenReturn(this.event1);
	when(this.ds.addNew(this.event2)).thenThrow(new PersistenceException());
	when(this.event1.getId()).thenReturn(KNOWN_EVENT_ID);
	when(this.ds.update(this.event1)).thenReturn(Boolean.TRUE);

	when(this.event1.getAdmins()).thenReturn(Collections.singletonList(this.user));
	when(this.user.getId()).thenReturn(USER_ID);
	TournamentEdition te = mock(TournamentEdition.class);
	when(this.event1.getTournamentEdition()).thenReturn(te);
	TournamentFormat tf = mock(TournamentFormat.class);
	when(te.getTournamentFormat()).thenReturn(tf);
	when(tf.getAdmins()).thenReturn(Collections.emptyList());

	when(this.ds.addDivisionToEdition(any(TournamentEdition.class),any(DivisionRegistration.class))).thenReturn(this.storedDiv);

	when(this.updateDiv.getId()).thenReturn(UPDATEABLE_DIV_ID);
	when(this.nonUpdateDiv.getId()).thenReturn(NON_UPDATEABLE_DIV_ID);
	when(this.ds.update(this.updateDiv)).thenReturn(true);
	when(this.ds.update(this.nonUpdateDiv)).thenReturn(false);
    }

    @Test
    public void testGetAllEventsNoDate() throws Exception {
	List<Event> allEvents = this.resource.getAllEvents(null, null);
	Assert.assertNotNull(allEvents);
	Assert.assertEquals(allEvents.size(), 3);
	Assert.assertTrue(allEvents.contains(this.event1));
	Assert.assertTrue(allEvents.contains(this.event2));
	Assert.assertTrue(allEvents.contains(this.event3));
    }

    @Test( expected = WebApplicationException.class )
    public void testGetAllEventsNoDS() throws Exception {
	this.resource.dataStore = null;
	this.resource.getAllEvents(null, null);
    }

    @Test
    public void testGetSpecificEvent() throws Exception {
	Event event = this.resource.getEvent(KNOWN_EVENT_ID);
	Assert.assertNotNull(event);
	Assert.assertEquals(event, this.event1);
    }

    @Test (expected = WebApplicationException.class )
    public void testGetSpecificUnknownEvent() throws Exception {
	this.resource.getEvent(UNKNOWN_EVENT_ID);
    }

    @Test (expected = WebApplicationException.class )
    public void testGetSpecificEventWithException() throws Exception {
	this.resource.getEvent(EXCEPTION_EVENT_ID);
    }

    @Test
    public void testCreateEvent() throws Exception {
	Event storedEvent = this.resource.createNewEvent(this.event1, this.user);
	Assert.assertSame(this.event1, storedEvent);
    }

    @Test(expected = WebApplicationException.class)
    public void testCreateEventFails() throws Exception {
	this.resource.createNewEvent(this.event2, this.user);
    }

    @Test
    public void testUpdateEventSuccess() throws Exception {
	this.resource.updateEvent(KNOWN_EVENT_ID, this.event1, this.user);
    }

    @Test( expected = WebApplicationException.class )
    public void testUpdateEventWrongEventID() throws Exception {
	this.resource.updateEvent(UNKNOWN_EVENT_ID, this.event1, this.user);
    }

    @Test(expected = WebApplicationException.class)
    public void testUpdateEventUnauthorized() throws Exception {
	User unauthorizedUser = mock(User.class);
	when(unauthorizedUser.getId()).thenReturn(USER_ID + 1);
	this.resource.updateEvent(KNOWN_EVENT_ID, this.event1, unauthorizedUser);
    }

    @Test
    public void testAddDivision() throws Exception {
	DivisionRegistration div = mock(DivisionRegistration.class);
	DivisionRegistration result = this.resource.addDivision(KNOWN_EVENT_ID, div, this.user);
	Assert.assertNotNull(result);
	Assert.assertSame(result, this.storedDiv);
	
    }

    @Test (expected = WebApplicationException.class)
    public void testAddDivionUnauthorized() throws Exception {
	DivisionRegistration div = mock(DivisionRegistration.class);
	this.resource.addDivision(UNKNOWN_EVENT_ID, div, this.user);
    }

    /*
     * TODO: add a test-case for a violated foreign-key constraint
     * (cf. addDivision in EventsResource). IN order to do this it
     * will be required to enhance the mocking, such that e.g. the id
     * of the 'fake edition' will be matched and a
     * PersistenceException will be thrown in that case. 
     */

    @Test
    public void testUpdateDivision() throws Exception {
	this.resource.updateDivsion(KNOWN_EVENT_ID, this.updateDiv, UPDATEABLE_DIV_ID, this.user);
    }

    @Test(expected = WebApplicationException.class)
    public void testUpdateDivsionWrongDivID() throws Exception {
	this.resource.updateDivsion(KNOWN_EVENT_ID, this.updateDiv, NON_UPDATEABLE_DIV_ID, this.user);
    }

    @Test(expected = WebApplicationException.class)
    public void testUpdateDivisionUnauthorized() throws Exception {
	this.resource.updateDivsion(UNKNOWN_EVENT_ID, this.updateDiv, UPDATEABLE_DIV_ID, this.user);
    }

    @Test(expected = WebApplicationException.class)
    public void testUpdateDivisionUpdateFails() throws Exception {
	this.resource.updateDivsion(KNOWN_EVENT_ID, this.nonUpdateDiv, NON_UPDATEABLE_DIV_ID, this.user);
    }
}
