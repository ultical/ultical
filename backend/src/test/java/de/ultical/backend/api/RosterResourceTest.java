package de.ultical.backend.api;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;

import javax.ws.rs.WebApplicationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.DivisionAge;
import de.ultical.backend.model.DivisionType;
import de.ultical.backend.model.Gender;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.Season;
import de.ultical.backend.model.Team;
import de.ultical.backend.model.User;

public class RosterResourceTest {

    private static final int USER_ID = 23;

    private static final int TEAM_42 = 42;

    private static final int DFV_NUMBER_MASTER = 1234567;
    private static final int DFV_NUMBER_JUNIOR = 1234568;
    private static final int DFV_NUMBER_WOMAN = 1234569;

    private final static int ROSTER_ID_MASTER = 123;
    private final static int ROSTER_ID_JUNIOR = 124;
    private final static int ROSTER_ID_WOMEN = 125;
    private final static int ROSTER_ID_OPEN_REG = 126;

    @Mock
    Roster rosterMaster;
    @Mock
    Roster rosterJunior;
    @Mock
    Roster rosterWomen;
    @Mock
    Roster rosterOpenRegular;
    @Mock
    Team team;
    @Mock
    User currentUser;
    @Mock
    DfvPlayer playerMasters;
    @Mock
    DfvPlayer playerJuniors;
    @Mock
    DfvPlayer playerWoman;
    @Mock
    DataStore dataStore;

    @Mock
    DfvMvName dfvNameMaster;
    @Mock
    DfvMvName dfvNameJunior;
    @Mock
    DfvMvName dfvNameWoman;

    private RosterResource resource;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Season season = new Season();
        season.setId(1);
        season.setYear(2016);

        when(this.rosterMaster.getId()).thenReturn(Integer.valueOf(ROSTER_ID_MASTER));
        when(this.rosterMaster.getSeason()).thenReturn(season);
        when(this.rosterMaster.getDivisionType()).thenReturn(DivisionType.OPEN);
        when(this.rosterMaster.getDivisionAge()).thenReturn(DivisionAge.MASTERS);
        when(this.dataStore.get(eq(ROSTER_ID_MASTER), eq(Roster.class))).thenReturn(this.rosterMaster);
        when(this.rosterJunior.getId()).thenReturn(Integer.valueOf(ROSTER_ID_JUNIOR));
        when(this.rosterJunior.getSeason()).thenReturn(season);
        when(this.rosterJunior.getDivisionType()).thenReturn(DivisionType.OPEN);
        when(this.rosterJunior.getDivisionAge()).thenReturn(DivisionAge.U23);
        when(this.dataStore.get(eq(ROSTER_ID_JUNIOR), eq(Roster.class))).thenReturn(this.rosterJunior);
        when(this.rosterWomen.getId()).thenReturn(Integer.valueOf(ROSTER_ID_WOMEN));
        when(this.rosterWomen.getSeason()).thenReturn(season);
        when(this.rosterWomen.getDivisionType()).thenReturn(DivisionType.WOMEN);
        when(this.rosterWomen.getDivisionAge()).thenReturn(DivisionAge.REGULAR);
        when(this.rosterWomen.getTeam()).thenReturn(this.team);
        when(this.dataStore.get(eq(ROSTER_ID_WOMEN), eq(Roster.class))).thenReturn(this.rosterWomen);
        when(this.rosterOpenRegular.getId()).thenReturn(ROSTER_ID_OPEN_REG);
        when(this.rosterOpenRegular.getDivisionAge()).thenReturn(DivisionAge.REGULAR);
        when(this.rosterOpenRegular.getDivisionType()).thenReturn(DivisionType.OPEN);
        when(this.rosterOpenRegular.getSeason()).thenReturn(season);
        when(this.rosterOpenRegular.getTeam()).thenReturn(this.team);
        when(this.dataStore.get(eq(ROSTER_ID_OPEN_REG), eq(Roster.class))).thenReturn(this.rosterOpenRegular);
        when(this.dataStore.get(eq(TEAM_42), eq(Team.class))).thenReturn(this.team);
        when(this.rosterMaster.getTeam()).thenReturn(this.team);
        when(this.rosterJunior.getTeam()).thenReturn(this.team);
        when(this.dfvNameMaster.getDfvNumber()).thenReturn(Integer.valueOf(DFV_NUMBER_MASTER));
        when(this.dfvNameMaster.isDse()).thenReturn(Boolean.TRUE);
        when(this.dataStore.getPlayerByDfvNumber(DFV_NUMBER_MASTER)).thenReturn(this.playerMasters);
        when(this.playerMasters.getBirthDate()).thenReturn(LocalDate.of(1983, 12, 31));
        when(this.playerMasters.getGender()).thenReturn(Gender.MALE);
        when(this.team.getId()).thenReturn(Integer.valueOf(TEAM_42));
        when(this.team.getAdmins()).thenReturn(Collections.singletonList(this.currentUser));
        when(this.currentUser.getId()).thenReturn(Integer.valueOf(USER_ID));

        when(this.dfvNameJunior.getDfvNumber()).thenReturn(Integer.valueOf(DFV_NUMBER_JUNIOR));
        when(this.dfvNameJunior.isDse()).thenReturn(Boolean.TRUE);
        when(this.dataStore.getPlayerByDfvNumber(DFV_NUMBER_JUNIOR)).thenReturn(this.playerJuniors);
        when(this.playerJuniors.getBirthDate()).thenReturn(LocalDate.of(1995, 5, 1));

        when(this.dfvNameWoman.getDfvNumber()).thenReturn(Integer.valueOf(DFV_NUMBER_WOMAN));
        when(this.dfvNameWoman.isDse()).thenReturn(Boolean.TRUE);
        when(this.dataStore.getPlayerByDfvNumber(DFV_NUMBER_WOMAN)).thenReturn(this.playerWoman);
        when(this.playerWoman.getGender()).thenReturn(Gender.FEMALE);
        when(this.playerWoman.getBirthDate()).thenReturn(LocalDate.of(1991, 3, 20));

        this.resource = new RosterResource();
        this.resource.dataStore = this.dataStore;
    }

    @After
    public void tearDown() {
        // Mockito.reset(this.dataStore);
    }

    @Test
    public void testAddMasterToMasters() throws Exception {
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_MASTER, this.dfvNameMaster);
        verify(this.dataStore).addPlayerToRoster(this.rosterMaster, this.playerMasters);
    }

    @Test
    public void testAddJuniorToMasters() throws Exception {
        this.expected.expect(WebApplicationException.class);
        this.expected.expectMessage("age does not match");
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_MASTER, this.dfvNameJunior);
        verify(this.dataStore, never()).addPlayerToRoster(any(), any());
    }

    @Test
    public void testAddJuniorToJunior() throws Exception {
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_JUNIOR, this.dfvNameJunior);
        verify(this.dataStore).addPlayerToRoster(this.rosterJunior, this.playerJuniors);
    }

    @Test
    public void testAddMasterToJuniors() throws Exception {
        this.expected.expect(WebApplicationException.class);
        this.expected.expectMessage("age does not match");
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_JUNIOR, this.dfvNameMaster);
        verify(this.dataStore, never()).addPlayerToRoster(any(), any());
    }

    @Test
    public void testAddMaleToWomenRoster() throws Exception {
        this.expected.expect(WebApplicationException.class);
        this.expected.expectMessage("has wrong gender");
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_WOMEN, this.dfvNameMaster);
        verify(this.dataStore, never()).addPlayerToRoster(any(), any());
    }

    @Test
    public void testAddWomanToWomen() throws Exception {
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_WOMEN, this.dfvNameWoman);
        verify(this.dataStore).addPlayerToRoster(this.rosterWomen, this.playerWoman);
    }

    @Test
    public void testAllCanPlayOpen() throws Exception {
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_OPEN_REG, this.dfvNameJunior);
        verify(this.dataStore).addPlayerToRoster(this.rosterOpenRegular, this.playerJuniors);
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_OPEN_REG, this.dfvNameMaster);
        verify(this.dataStore).addPlayerToRoster(this.rosterOpenRegular, this.playerMasters);
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_OPEN_REG, this.dfvNameWoman);
        verify(this.dataStore).addPlayerToRoster(this.rosterOpenRegular, this.playerWoman);
    }
}
