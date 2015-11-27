package de.ultical.backend.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.mockito.*;

import de.ultical.backend.model.Event;
import de.ultical.backend.model.TournamentEdition;

public class DataStoreTest {

	private DataStore ds;
	@Mock
	TournamentEdition mockedTournament;
	@Mock
	TournamentEdition completeTournament;
	@Mock
	Event eventStartDatenull;
	@Mock
	Event mockOnlyEvent;
	@Mock Event wrongTournamentEvent;

	@Before
	public void setUp() {
		this.ds = new DataStore();
		MockitoAnnotations.initMocks(this);
		when(completeTournament.getAlternativeName()).thenReturn("FooBar Tournament");
		TournamentEdition tournamenMock = mock(TournamentEdition.class);
		when(tournamenMock.getEvents()).thenReturn(Collections.singletonList(eventStartDatenull));
		when(eventStartDatenull.getTournamentEdition()).thenReturn(tournamenMock);
		when(wrongTournamentEvent.getTournamentEdition()).thenReturn(tournamenMock);
	}

	@Test(expected = NullPointerException.class)
	public void testStoreTournamentNull() throws Exception {
		this.ds.storeTournament(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStoreTournamentNameNull() throws Exception {
		this.ds.storeTournament(mockedTournament);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStoreTournamentNameEmpty() throws Exception {
		when(mockedTournament.getAlternativeName()).thenReturn("");
		this.ds.storeTournament(mockedTournament);
	}

	@Test
	public void testStoreTournamentSuccess() throws Exception {
		Assert.assertTrue(this.ds.storeTournament(completeTournament));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStoreTournamentNameExists() throws Exception {
		this.ds.storeTournament(completeTournament);
		TournamentEdition otherTournament = Mockito.mock(TournamentEdition.class);
		when(otherTournament.getAlternativeName()).thenReturn("FooBar Tournament");
		this.ds.storeTournament(otherTournament);
	}

	@Test(expected = NullPointerException.class)
	public void testGetTournamentNameNull() throws Exception {
		this.ds.getTournamentByName(null);
	}

	@Test
	public void testGetTournamentNameUnknown() throws Exception {
		TournamentEdition t = this.ds.getTournamentByName("unknown Tournament");
		Assert.assertNull(t);
	}

	@Test
	public void testStoreAndGetTournament() throws Exception {
		this.ds.storeTournament(completeTournament);
		TournamentEdition t = this.ds.getTournamentByName("FooBar Tournament");
		Assert.assertNotNull(t);
		Assert.assertThat(t.getAlternativeName(), CoreMatchers.equalTo("FooBar Tournament"));
	}

	@Test(expected = NullPointerException.class)
	public void testStoreEventNull() throws Exception {
		this.ds.storeEvent(null);
	}

	@Test(expected = NullPointerException.class)
	public void testStoreEventStartDateNull() throws Exception {
		this.ds.storeEvent(eventStartDatenull);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testStoreEventWrongTournament() throws Exception {
		this.ds.storeEvent(wrongTournamentEvent);
	}

}
