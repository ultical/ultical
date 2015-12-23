package de.ultical.backend.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.apache.ibatis.session.SqlSession;
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
    @Mock
    SqlSession sqlSession;

    @Before
    public void setUp() {
        this.ds = new DataStore();
        MockitoAnnotations.initMocks(this);
        when(this.completeTournament.getAlternativeName()).thenReturn("FooBar Tournament");
        TournamentEditionSingle tournamenMock = mock(TournamentEditionSingle.class);
        when(tournamenMock.getEvent()).thenReturn(this.eventStartDatenull);
        when(this.eventStartDatenull.getTournamentEdition()).thenReturn(tournamenMock);
        when(this.wrongTournamentEvent.getTournamentEdition()).thenReturn(tournamenMock);

        this.ds.sqlSession = this.sqlSession;
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
    public void testCloseSession() {
        assertFalse(this.ds.closeSession());
        verify(this.sqlSession, never()).close();
        this.ds.setAutoCloseSession(false);
        assertTrue(this.ds.closeSession());
        verify(this.sqlSession, times(1)).close();
    }

    @Test
    public void testCloseSessionSessionNull() {
        this.ds.sqlSession = null;
        this.ds.setAutoCloseSession(false);
        assertFalse(this.ds.closeSession());
    }
}
