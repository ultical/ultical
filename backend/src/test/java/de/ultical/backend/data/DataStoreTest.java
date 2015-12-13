package de.ultical.backend.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.ultical.backend.model.*;

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
	@Mock
	Event wrongTournamentEvent;

	@Before
	public void setUp() {
		this.ds = new DataStore();
		MockitoAnnotations.initMocks(this);
		when(completeTournament.getAlternativeName()).thenReturn("FooBar Tournament");
		TournamentEditionSingle tournamenMock = mock(TournamentEditionSingle.class);
		when(tournamenMock.getEvent()).thenReturn(eventStartDatenull);
		when(eventStartDatenull.getTournamentEdition()).thenReturn(tournamenMock);
		when(wrongTournamentEvent.getTournamentEdition()).thenReturn(tournamenMock);
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

}
