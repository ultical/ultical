package de.ultical.backend.api;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.data.DataStore.DataStoreCloseable;
import de.ultical.backend.exception.IBANValidationException;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.Team;
import de.ultical.backend.model.TeamRegistration;
import de.ultical.backend.model.User;
import de.ultical.backend.services.IBANValidationService;
import org.mockito.Mock;
import org.junit.Before;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;
import java.util.Collections;
import org.junit.Test;
import javax.ws.rs.WebApplicationException;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.apache.ibatis.exceptions.PersistenceException;

public class DivisionResourceTest {

    private static final int ROSTER_ID = 42;
    private static final int USER_ID = 43;
    private static final int TEAM_ID = 44;
    private static final int DIVISION_ID = 1;
    private static final String INVALID_IBAN = "DE6254321";
    @Mock
    DataStore dataStore;
    @Mock
    User authorizedUser;
    @Mock
    User unauthorizedUser;
    @Mock
    TeamRegistration teamRegWithoutRoster;
    @Mock
    TeamRegistration teamRegWithRoster;

    @Rule
    public ExpectedException expected = ExpectedException.none();
    
    private DivisionResource resource;

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);

	Mockito.when(authorizedUser.getId()).thenReturn(USER_ID);
	Mockito.when(unauthorizedUser.getId()).thenReturn(USER_ID + 23);
	Roster mockedRoster = Mockito.mock(Roster.class);
	Mockito.when(mockedRoster.getId()).thenReturn(ROSTER_ID);
	Mockito.when(teamRegWithRoster.getRoster()).thenReturn(mockedRoster);
	Mockito.when(teamRegWithRoster.getIban()).thenReturn("DE6112345");
	Mockito.when(teamRegWithoutRoster.getIban()).thenReturn("DE6112345");

	Mockito.when(this.dataStore.getClosable()).thenReturn(Mockito.mock(DataStoreCloseable.class));
	Mockito.when(this.dataStore.get(ROSTER_ID,Roster.class)).thenReturn(mockedRoster);

	Team mockedTeam = Mockito.mock(Team.class);
	Mockito.when(mockedRoster.getTeam()).thenReturn(mockedTeam);
	Mockito.when(mockedTeam.getId()).thenReturn(TEAM_ID);
	Mockito.when(mockedTeam.getAdmins()).thenReturn(Collections.singletonList(this.authorizedUser));
	Mockito.when(this.dataStore.get(TEAM_ID,Team.class)).thenReturn(mockedTeam);

	Mockito.when(this.dataStore.registerTeamForEdition(Mockito.eq(2),Mockito.any(TeamRegistration.class))).thenThrow(Mockito.mock(PersistenceException.class));

	IBANValidationService ibanService = Mockito.mock(IBANValidationService.class);
	Mockito.doThrow(Mockito.mock(IBANValidationException.class)).when(ibanService).validateIBAN(Mockito.eq(INVALID_IBAN));
	this.resource = new DivisionResource(this.dataStore, ibanService);
    }


    @Test
    public void testRegisterWrongRosterId() throws Exception {
	this.expected.expect(WebApplicationException.class);
	this.expected.expectMessage("payload do not match");
	this.resource.registerTeam(authorizedUser,DIVISION_ID,2,this.teamRegWithRoster);
	Mockito.verify(this.dataStore,Mockito.never()).registerTeamForEdition(DIVISION_ID,this.teamRegWithRoster);
    }

    @Test
    public void testUnauthorizedUser() throws Exception {
	this.expected.expect(WebApplicationException.class);
	this.expected.expectMessage("not an admin for team");
	this.resource.registerTeam(unauthorizedUser,DIVISION_ID, ROSTER_ID, this.teamRegWithoutRoster);
	Mockito.verify(this.dataStore,Mockito.never()).registerTeamForEdition(DIVISION_ID,this.teamRegWithoutRoster);
    }

    @Test
    public void testSuccessfullRegistration() throws Exception {
	this.resource.registerTeam(authorizedUser,DIVISION_ID, ROSTER_ID, this.teamRegWithRoster);
	Mockito.verify(this.dataStore, Mockito.times(1)).registerTeamForEdition(DIVISION_ID, this.teamRegWithRoster);
    }

    @Test public void testFailureDuringSave() throws Exception {
	this.expected.expect(WebApplicationException.class);
	this.expected.expectMessage("database failed");
	this.resource.registerTeam(authorizedUser,2, ROSTER_ID, this.teamRegWithRoster);
    }

    @Test
    public void testRegisterWithoutIban() throws Exception {
	Mockito.when(this.teamRegWithRoster.getIban()).thenReturn(null);
	this.expected.expect(WebApplicationException.class);
	this.expected.expectMessage("IBAN must be provided");

	this.resource.registerTeam(authorizedUser,DIVISION_ID,ROSTER_ID,this.teamRegWithRoster);
    }

    @Test
    public void testRegisterWithInvalidIban() throws Exception {
	Mockito.when(this.teamRegWithRoster.getIban()).thenReturn(INVALID_IBAN);
	this.expected.expect(WebApplicationException.class);
	this.expected.expectMessage("IBAN is invalid");

	this.resource.registerTeam(authorizedUser, DIVISION_ID, ROSTER_ID, this.teamRegWithRoster);
    }

    @Test
    public void testSuccessfullUnregister() throws Exception {
	this.resource.unregisterTeam(authorizedUser, DIVISION_ID, ROSTER_ID);
	Mockito.verify(this.dataStore,Mockito.times(1)).unregisterTeamFromDivision(Mockito.any(),Mockito.any());
    }
}
