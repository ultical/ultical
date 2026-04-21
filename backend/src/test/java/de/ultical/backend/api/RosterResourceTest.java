package de.ultical.backend.api;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.api.transferClasses.DfvMvPlayer;
import de.ultical.backend.app.DfvApiConfig;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.Context;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.DivisionAge;
import de.ultical.backend.model.DivisionType;
import de.ultical.backend.model.Gender;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.RosterPlayer;
import de.ultical.backend.model.Season;
import de.ultical.backend.model.Team;
import de.ultical.backend.model.TeamRegistration;
import de.ultical.backend.model.User;

public class RosterResourceTest {

    private static final int DFV_NUMBER_PASSIVE = 12344321;

    private static final int USER_ID = 23;

    private static final int TEAM_42 = 42;
    private static final int TEAM_43 = 43;

    private static final int DFV_NUMBER_MASTER = 1234567;
    private static final int DFV_NUMBER_JUNIOR = 1234568;
    private static final int DFV_NUMBER_WOMAN = 1234569;
    private static final int DFV_NUMBER_17YO_WOMAN = 1234570;
    private static final int DFV_NUMBER_UNPAID_PLAYER = 567890;
    private static final int DFV_NUMBER_DIVERSE = 1234571;
    private static final int DFV_NUMBER_DIVERSE_30YO = 1234572;
    private static final int DFV_NUMBER_DIVERSE_37YO = 1234573;
    private static final int DFV_NUMBER_DIVERSE_45YO = 1234574;
    private static final int DFV_NUMBER_MALE_37YO = 1234575;
    private static final int DFV_NUMBER_MALE_45YO = 1234576;

    private final static int ROSTER_ID_MASTER = 123;
    private final static int ROSTER_ID_JUNIOR = 124;
    private final static int ROSTER_ID_WOMEN = 125;
    private final static int ROSTER_ID_OPEN_REG_A = 126;
    private final static int ROSTER_ID_OPEN_REG_B = 127;
    private final static int ROSTER_ID_OPEN_U17 = 128;
    private final static int ROSTER_ID_MIXED = 129;
    private final static int ROSTER_ID_GRANDMASTERS = 130;
    private final static int ROSTER_ID_GREATGRAND = 131;

    private final static int TEAM_REG_A = 2245;

    @Mock
    Roster rosterMaster;
    @Mock
    Roster rosterJunior;
    @Mock
    Roster rosterWomen;
    @Mock
    Roster rosterOpenRegularA;
    @Mock
    Roster rosterOpenRegularB;
    @Mock
    Roster rosterU17Open;
    @Mock
    Team teamA;
    @Mock
    Team teamB;
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
    TeamRegistration teamRegA;
    @Mock
    DfvMvName dfvNameMaster;
    @Mock
    DfvMvName dfvNameJunior;
    @Mock
    DfvMvName dfvNameWoman;
    @Mock
    DfvMvName dfvName17yoWoman;
    @Mock
    DfvPlayer player17yoWoman;
    @Mock
    DfvPlayer passivePlayer;
    @Mock
    DfvMvName dfvNamePassive;
    @Mock
    DfvMvName dfvUnpaidPlayer;
    @Mock
    Roster rosterMixed;
    @Mock
    DfvPlayer playerDiverse;
    @Mock
    DfvMvName dfvNameDiverse;
    @Mock
    DfvPlayer playerDiverse30yo;
    @Mock
    DfvMvName dfvNameDiverse30yo;
    @Mock
    Roster rosterGrandmasters;
    @Mock
    Roster rosterGreatgrand;
    @Mock
    DfvPlayer playerDiverse37yo;
    @Mock
    DfvMvName dfvNameDiverse37yo;
    @Mock
    DfvPlayer playerDiverse45yo;
    @Mock
    DfvMvName dfvNameDiverse45yo;
    @Mock
    DfvPlayer playerMale37yo;
    @Mock
    DfvMvName dfvNameMale37yo;
    @Mock
    DfvPlayer playerMale45yo;
    @Mock
    DfvMvName dfvNameMale45yo;

    private RosterResource resource;

    private Season season;
    private Context dfvContext;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        this.season = new Season();
        this.season.setId(1);
        this.season.setYear(2016);

        this.dfvContext = new Context();
        this.dfvContext.setId(1);
        this.dfvContext.setAcronym("DFV");

        when(this.rosterMaster.getId()).thenReturn(Integer.valueOf(ROSTER_ID_MASTER));
        when(this.rosterMaster.getSeason()).thenReturn(this.season);
        when(this.rosterMaster.getDivisionType()).thenReturn(DivisionType.OPEN);
        when(this.rosterMaster.getDivisionAge()).thenReturn(DivisionAge.MASTERS);
        when(this.dataStore.get(eq(ROSTER_ID_MASTER), eq(Roster.class))).thenReturn(this.rosterMaster);
        when(this.rosterJunior.getId()).thenReturn(Integer.valueOf(ROSTER_ID_JUNIOR));
        when(this.rosterJunior.getSeason()).thenReturn(this.season);
        when(this.rosterJunior.getDivisionType()).thenReturn(DivisionType.OPEN);
        when(this.rosterJunior.getDivisionAge()).thenReturn(DivisionAge.U23);
        when(this.dataStore.get(eq(ROSTER_ID_JUNIOR), eq(Roster.class))).thenReturn(this.rosterJunior);
        when(this.rosterWomen.getId()).thenReturn(Integer.valueOf(ROSTER_ID_WOMEN));
        when(this.rosterWomen.getSeason()).thenReturn(this.season);
        when(this.rosterWomen.getDivisionType()).thenReturn(DivisionType.WOMEN);
        when(this.rosterWomen.getDivisionAge()).thenReturn(DivisionAge.REGULAR);
        when(this.rosterWomen.getTeam()).thenReturn(this.teamA);
        when(this.dataStore.get(eq(ROSTER_ID_WOMEN), eq(Roster.class))).thenReturn(this.rosterWomen);
        when(this.rosterOpenRegularA.getId()).thenReturn(ROSTER_ID_OPEN_REG_A);
        when(this.rosterOpenRegularA.getDivisionAge()).thenReturn(DivisionAge.REGULAR);
        when(this.rosterOpenRegularA.getDivisionType()).thenReturn(DivisionType.OPEN);
        when(this.rosterOpenRegularA.getSeason()).thenReturn(this.season);
        when(this.rosterOpenRegularA.getTeam()).thenReturn(this.teamA);
        when(this.rosterOpenRegularA.getContext()).thenReturn(this.dfvContext);
        when(this.rosterOpenRegularA.getNameAddition()).thenReturn("");
        when(this.rosterOpenRegularB.getId()).thenReturn(ROSTER_ID_OPEN_REG_B);
        when(this.rosterOpenRegularB.getDivisionAge()).thenReturn(DivisionAge.REGULAR);
        when(this.rosterOpenRegularB.getDivisionType()).thenReturn(DivisionType.OPEN);
        when(this.rosterOpenRegularB.getSeason()).thenReturn(this.season);
        when(this.rosterOpenRegularB.getTeam()).thenReturn(this.teamB);
        when(this.rosterOpenRegularB.getContext()).thenReturn(this.dfvContext);
        when(this.rosterOpenRegularB.getNameAddition()).thenReturn("");
        when(this.rosterU17Open.getId()).thenReturn(ROSTER_ID_OPEN_U17);
        when(this.rosterU17Open.getDivisionType()).thenReturn(DivisionType.OPEN);
        when(this.rosterU17Open.getDivisionAge()).thenReturn(DivisionAge.U17);
        when(this.rosterU17Open.getSeason()).thenReturn(this.season);
        when(this.rosterU17Open.getContext()).thenReturn(this.dfvContext);
        when(this.rosterU17Open.getTeam()).thenReturn(this.teamA);
        when(this.rosterU17Open.getNameAddition()).thenReturn("");
        when(this.dataStore.get(eq(ROSTER_ID_OPEN_U17), eq(Roster.class))).thenReturn(this.rosterU17Open);
        when(this.dataStore.get(eq(ROSTER_ID_OPEN_REG_A), eq(Roster.class))).thenReturn(this.rosterOpenRegularA);
        when(this.dataStore.get(eq(ROSTER_ID_OPEN_REG_B), eq(Roster.class))).thenReturn(this.rosterOpenRegularB);
        when(this.dataStore.get(eq(TEAM_42), eq(Team.class))).thenReturn(this.teamA);
        when(this.dataStore.get(eq(TEAM_43), eq(Team.class))).thenReturn(this.teamB);
        when(this.rosterMaster.getTeam()).thenReturn(this.teamA);
        when(this.rosterJunior.getTeam()).thenReturn(this.teamA);
        when(this.dfvNameMaster.getDfvNumber()).thenReturn(Integer.valueOf(DFV_NUMBER_MASTER));
        when(this.dfvNameMaster.isDse()).thenReturn(Boolean.TRUE);
        when(this.dataStore.getDfvMvName(DFV_NUMBER_MASTER)).thenReturn(dfvNameMaster);
        when(this.dataStore.getPlayerByDfvNumber(DFV_NUMBER_MASTER)).thenReturn(this.playerMasters);
        when(this.playerMasters.getBirthDate()).thenReturn(LocalDate.of(1983, 12, 31));
        when(this.playerMasters.getGender()).thenReturn(Gender.MALE);
        when(this.playerMasters.isEligible()).thenReturn(Boolean.TRUE);
        when(this.teamA.getId()).thenReturn(Integer.valueOf(TEAM_42));
        when(this.teamA.getAdmins()).thenReturn(Collections.singletonList(this.currentUser));
        when(this.teamB.getId()).thenReturn(Integer.valueOf(TEAM_43));
        when(this.teamB.getAdmins()).thenReturn(Collections.singletonList(this.currentUser));
        when(this.currentUser.getId()).thenReturn(Integer.valueOf(USER_ID));

        when(this.dfvNameJunior.getDfvNumber()).thenReturn(Integer.valueOf(DFV_NUMBER_JUNIOR));
        when(this.dfvNameJunior.isDse()).thenReturn(Boolean.TRUE);
        when(this.dataStore.getDfvMvName(DFV_NUMBER_JUNIOR)).thenReturn(this.dfvNameJunior);
        when(this.dataStore.getPlayerByDfvNumber(DFV_NUMBER_JUNIOR)).thenReturn(this.playerJuniors);
        when(this.playerJuniors.getBirthDate()).thenReturn(LocalDate.of(1995, 5, 1));
        when(this.playerJuniors.getGender()).thenReturn(Gender.MALE);
        when(this.playerJuniors.isEligible()).thenReturn(Boolean.TRUE);

        when(this.dfvNameWoman.getDfvNumber()).thenReturn(Integer.valueOf(DFV_NUMBER_WOMAN));
        when(this.dfvNameWoman.isDse()).thenReturn(Boolean.TRUE);
        when(this.playerWoman.getGender()).thenReturn(Gender.FEMALE);
        when(this.playerWoman.getBirthDate()).thenReturn(LocalDate.of(1991, 3, 20));
        when(this.playerWoman.isEligible()).thenReturn(Boolean.TRUE);
        when(this.dataStore.getDfvMvName(DFV_NUMBER_WOMAN)).thenReturn(dfvNameWoman);
        when(this.dataStore.getPlayerByDfvNumber(DFV_NUMBER_WOMAN)).thenReturn(this.playerWoman);

        when(this.teamRegA.getId()).thenReturn(TEAM_REG_A);
        when(this.teamRegA.getRoster()).thenReturn(this.rosterOpenRegularA);

        when(this.passivePlayer.getId()).thenReturn(DFV_NUMBER_PASSIVE);
        when(this.dataStore.getPlayerByDfvNumber(DFV_NUMBER_PASSIVE)).thenReturn(this.passivePlayer);
        when(this.dfvNamePassive.getDfvNumber()).thenReturn(DFV_NUMBER_PASSIVE);
        when(this.dfvNamePassive.isDse()).thenReturn(Boolean.TRUE);
        when(this.dataStore.getDfvMvName(DFV_NUMBER_PASSIVE)).thenReturn(dfvNamePassive);

        when(this.dfvName17yoWoman.getDfvNumber()).thenReturn(DFV_NUMBER_17YO_WOMAN);
        when(this.dfvName17yoWoman.isDse()).thenReturn(Boolean.TRUE);
        when(this.dfvName17yoWoman.isActive()).thenReturn(Boolean.TRUE);
        when(this.player17yoWoman.getGender()).thenReturn(Gender.FEMALE);
        when(this.player17yoWoman.isEligible()).thenReturn(Boolean.TRUE);
        when(this.player17yoWoman.getBirthDate()).thenReturn(LocalDate.of(1999, 5, 6));
        when(this.dataStore.getDfvMvName(DFV_NUMBER_17YO_WOMAN)).thenReturn(dfvName17yoWoman);
        when(this.dataStore.getPlayerByDfvNumber(eq(DFV_NUMBER_17YO_WOMAN))).thenReturn(this.player17yoWoman);
        
        when(this.dfvUnpaidPlayer.getDfvNumber()).thenReturn(DFV_NUMBER_UNPAID_PLAYER);
        when(this.dfvUnpaidPlayer.isDse()).thenReturn(true);
        when(this.dfvUnpaidPlayer.isActive()).thenReturn(true);
        when(this.dfvUnpaidPlayer.getLastModified()).thenReturn(LocalDateTime.of(2018, 12, 27, 13,14,15));
        when(this.dataStore.getDfvMvName(DFV_NUMBER_UNPAID_PLAYER)).thenReturn(dfvUnpaidPlayer);

        DfvMvPlayer unpaidPlayer = new DfvMvPlayer();
        unpaidPlayer.setDse(true);
        unpaidPlayer.setAv(true);
        unpaidPlayer.setActive(true);
        unpaidPlayer.setDfvnr(DFV_NUMBER_UNPAID_PLAYER);
        unpaidPlayer.setGender("männlich");
        unpaidPlayer.setEmail("test");
        unpaidPlayer.setIdle(false);
        unpaidPlayer.setPaid(false);
        unpaidPlayer.setDobString("1981-02-03");

        Invocation.Builder builder = Mockito.mock(Invocation.Builder.class);
        when(builder.get(DfvMvPlayer.class)).thenReturn(unpaidPlayer);
        WebTarget target = Mockito.mock(WebTarget.class);
        when(target.path(Mockito.anyString())).thenReturn(target);
        when(target.queryParam(Mockito.anyString(), Mockito.any())).thenReturn(target);
        when(target.path(Mockito.anyString())).thenReturn(target);
        when(target.request(Mockito.eq(MediaType.APPLICATION_JSON))).thenReturn(builder);
        Client client = Mockito.mock(Client.class);
        when(client.target(Mockito.anyString())).thenReturn(target);
        
        UltiCalConfig conf = new UltiCalConfig();
        DfvApiConfig dfvApi = new DfvApiConfig();
        dfvApi.setUrl("dfdf");
        dfvApi.setToken("dfdf");
        dfvApi.setSecret("dfdfd");
		conf.setDfvApi(dfvApi);

        when(this.rosterMixed.getId()).thenReturn(ROSTER_ID_MIXED);
        when(this.rosterMixed.getSeason()).thenReturn(this.season);
        when(this.rosterMixed.getDivisionType()).thenReturn(DivisionType.MIXED);
        when(this.rosterMixed.getDivisionAge()).thenReturn(DivisionAge.REGULAR);
        when(this.rosterMixed.getTeam()).thenReturn(this.teamA);
        when(this.dataStore.get(eq(ROSTER_ID_MIXED), eq(Roster.class))).thenReturn(this.rosterMixed);

        when(this.rosterGrandmasters.getId()).thenReturn(ROSTER_ID_GRANDMASTERS);
        when(this.rosterGrandmasters.getSeason()).thenReturn(this.season);
        when(this.rosterGrandmasters.getDivisionType()).thenReturn(DivisionType.OPEN);
        when(this.rosterGrandmasters.getDivisionAge()).thenReturn(DivisionAge.GRANDMASTERS);
        when(this.rosterGrandmasters.getTeam()).thenReturn(this.teamA);
        when(this.dataStore.get(eq(ROSTER_ID_GRANDMASTERS), eq(Roster.class))).thenReturn(this.rosterGrandmasters);

        when(this.rosterGreatgrand.getId()).thenReturn(ROSTER_ID_GREATGRAND);
        when(this.rosterGreatgrand.getSeason()).thenReturn(this.season);
        when(this.rosterGreatgrand.getDivisionType()).thenReturn(DivisionType.OPEN);
        when(this.rosterGreatgrand.getDivisionAge()).thenReturn(DivisionAge.GREATGRAND);
        when(this.rosterGreatgrand.getTeam()).thenReturn(this.teamA);
        when(this.dataStore.get(eq(ROSTER_ID_GREATGRAND), eq(Roster.class))).thenReturn(this.rosterGreatgrand);

        // Bonus-tier test players. Calendar age in 2016 season = 2016 - birth year.
        // GRANDMASTERS threshold is 40, GREATGRAND is 48. With the +3 bonus,
        // FEMALE/DIVERSE qualify at calendar age 37 / 45; MALE/NA don't.
        setupPlayerAndName(this.playerDiverse37yo, this.dfvNameDiverse37yo, DFV_NUMBER_DIVERSE_37YO,
                Gender.DIVERSE, LocalDate.of(1979, 6, 15));
        setupPlayerAndName(this.playerDiverse45yo, this.dfvNameDiverse45yo, DFV_NUMBER_DIVERSE_45YO,
                Gender.DIVERSE, LocalDate.of(1971, 6, 15));
        setupPlayerAndName(this.playerMale37yo, this.dfvNameMale37yo, DFV_NUMBER_MALE_37YO,
                Gender.MALE, LocalDate.of(1979, 6, 15));
        setupPlayerAndName(this.playerMale45yo, this.dfvNameMale45yo, DFV_NUMBER_MALE_45YO,
                Gender.MALE, LocalDate.of(1971, 6, 15));

        when(this.dfvNameDiverse.getDfvNumber()).thenReturn(DFV_NUMBER_DIVERSE);
        when(this.dfvNameDiverse.isDse()).thenReturn(Boolean.TRUE);
        when(this.playerDiverse.getGender()).thenReturn(Gender.DIVERSE);
        when(this.playerDiverse.getBirthDate()).thenReturn(LocalDate.of(1990, 6, 15));
        when(this.playerDiverse.isEligible()).thenReturn(Boolean.TRUE);
        when(this.dataStore.getDfvMvName(DFV_NUMBER_DIVERSE)).thenReturn(this.dfvNameDiverse);
        when(this.dataStore.getPlayerByDfvNumber(DFV_NUMBER_DIVERSE)).thenReturn(this.playerDiverse);

        // 30 years old in the 2016 test season: below masters cutoff for male,
        // but inside it once the female/diverse +3 year bonus is applied.
        when(this.dfvNameDiverse30yo.getDfvNumber()).thenReturn(DFV_NUMBER_DIVERSE_30YO);
        when(this.dfvNameDiverse30yo.isDse()).thenReturn(Boolean.TRUE);
        when(this.playerDiverse30yo.getGender()).thenReturn(Gender.DIVERSE);
        when(this.playerDiverse30yo.getBirthDate()).thenReturn(LocalDate.of(1986, 6, 15));
        when(this.playerDiverse30yo.isEligible()).thenReturn(Boolean.TRUE);
        when(this.dataStore.getDfvMvName(DFV_NUMBER_DIVERSE_30YO)).thenReturn(this.dfvNameDiverse30yo);
        when(this.dataStore.getPlayerByDfvNumber(DFV_NUMBER_DIVERSE_30YO)).thenReturn(this.playerDiverse30yo);

        this.resource = new RosterResource();
        this.resource.dataStore = this.dataStore;
        this.resource.client = client;
        this.resource.config = conf;
    }

    @After
    public void tearDown() {
        // Mockito.reset(this.dataStore);
    }

    private void setupPlayerAndName(DfvPlayer player, DfvMvName name, int dfvNumber, Gender gender,
            LocalDate birthDate) {
        when(name.getDfvNumber()).thenReturn(dfvNumber);
        when(name.isDse()).thenReturn(Boolean.TRUE);
        when(player.getGender()).thenReturn(gender);
        when(player.getBirthDate()).thenReturn(birthDate);
        when(player.isEligible()).thenReturn(Boolean.TRUE);
        when(this.dataStore.getDfvMvName(dfvNumber)).thenReturn(name);
        when(this.dataStore.getPlayerByDfvNumber(dfvNumber)).thenReturn(player);
    }

    @Test
    public void testAddPlayerToTwoRosters() throws Exception {
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_OPEN_REG_A, this.dfvNameMaster);
        verify(this.dataStore).addPlayerToRoster(this.rosterOpenRegularA, this.playerMasters);
        when(this.dataStore.getRosterByPlayerSeasonDivision(this.playerMasters.getId(), this.rosterOpenRegularB))
                .thenReturn(Collections.singletonList(this.rosterOpenRegularA));
        when(this.dataStore.getTeamRegistrationsByRoster(this.rosterOpenRegularA))
                .thenReturn(Collections.singletonList(this.teamRegA));
        this.expected.expect(WebApplicationException.class);
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_OPEN_REG_B, this.dfvNameMaster);
        verify(this.dataStore, times(1)).addPlayerToRoster(any(), any());
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

    @Test(expected = WebApplicationException.class)
    public void testAddPassivePlayer() throws Exception {
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_OPEN_REG_A, this.dfvNamePassive);
    }

    @Test(expected = WebApplicationException.class)
    public void testNoDse() throws Exception {
        final DfvMvName noDseMVName = new DfvMvName();
        noDseMVName.setDse(false);
        noDseMVName.setDfvnr(191919);
        when(this.dataStore.getDfvMvName(191919)).thenReturn(noDseMVName);
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_OPEN_REG_A, noDseMVName);
    }

    @Test
    public void testAllCanPlayOpen() throws Exception {
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_OPEN_REG_A, this.dfvNameJunior);
        verify(this.dataStore).addPlayerToRoster(this.rosterOpenRegularA, this.playerJuniors);
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_OPEN_REG_A, this.dfvNameMaster);
        verify(this.dataStore).addPlayerToRoster(this.rosterOpenRegularA, this.playerMasters);
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_OPEN_REG_A, this.dfvNameWoman);
        verify(this.dataStore).addPlayerToRoster(this.rosterOpenRegularA, this.playerWoman);
    }

    @Test
    public void test17yoWomanCannotPlayU17() throws Exception {
        this.expected.expect(WebApplicationException.class);
        this.expected.expectMessage("age does not match");
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_OPEN_U17, this.dfvName17yoWoman);
        verify(this.dataStore, never()).addPlayerToRoster(any(), any());
    }
    
    @Test(expected = WebApplicationException.class)
    public void testUnpaidPlayerCannotPlay() throws Exception {
    	this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_OPEN_REG_A, this.dfvUnpaidPlayer);
    }

    @Test
    public void testAddDiverseToOpen() throws Exception {
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_OPEN_REG_A, this.dfvNameDiverse);
        verify(this.dataStore).addPlayerToRoster(this.rosterOpenRegularA, this.playerDiverse);
    }

    @Test
    public void testAddDiverseToMixed() throws Exception {
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_MIXED, this.dfvNameDiverse);
        verify(this.dataStore).addPlayerToRoster(this.rosterMixed, this.playerDiverse);
    }

    @Test
    public void testAddDiverseToWomen() throws Exception {
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_WOMEN, this.dfvNameDiverse);
        verify(this.dataStore).addPlayerToRoster(this.rosterWomen, this.playerDiverse);
    }

    @Test
    public void testAddMaleToMixed() throws Exception {
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_MIXED, this.dfvNameMaster);
        verify(this.dataStore).addPlayerToRoster(this.rosterMixed, this.playerMasters);
    }

    @Test
    public void testAddWomanToMixed() throws Exception {
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_MIXED, this.dfvNameWoman);
        verify(this.dataStore).addPlayerToRoster(this.rosterMixed, this.playerWoman);
    }

    @Test
    public void testAddDiverseMasterGetsAgeBonus() throws Exception {
        // Player is 30 in the 2016 season: a MALE at that age would fail the
        // masters cut-off, but DIVERSE players get the same +3 year bonus as
        // FEMALE players, so they are allowed in the masters division.
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_MASTER, this.dfvNameDiverse30yo);
        verify(this.dataStore).addPlayerToRoster(this.rosterMaster, this.playerDiverse30yo);
    }

    @Test
    public void testAddDiverseGrandmasterGetsAgeBonus() throws Exception {
        // 37yo DIVERSE: below the GRANDMASTERS threshold of 40, but inside it
        // with the +3 bonus (37 + 3 = 40).
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_GRANDMASTERS, this.dfvNameDiverse37yo);
        verify(this.dataStore).addPlayerToRoster(this.rosterGrandmasters, this.playerDiverse37yo);
    }

    @Test
    public void testAddMale37yoToGrandmastersFails() throws Exception {
        // Same age as the diverse case above, but MALE gets no bonus and is
        // therefore rejected.
        this.expected.expect(WebApplicationException.class);
        this.expected.expectMessage("age does not match");
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_GRANDMASTERS, this.dfvNameMale37yo);
        verify(this.dataStore, never()).addPlayerToRoster(any(), any());
    }

    @Test
    public void testAddDiverseGreatgrandGetsAgeBonus() throws Exception {
        // 45yo DIVERSE: below the GREATGRAND threshold of 48, but inside it
        // with the +3 bonus (45 + 3 = 48).
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_GREATGRAND, this.dfvNameDiverse45yo);
        verify(this.dataStore).addPlayerToRoster(this.rosterGreatgrand, this.playerDiverse45yo);
    }

    @Test
    public void testAddMale45yoToGreatgrandFails() throws Exception {
        // Same age as the diverse case above, but MALE gets no bonus and is
        // therefore rejected.
        this.expected.expect(WebApplicationException.class);
        this.expected.expectMessage("age does not match");
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_GREATGRAND, this.dfvNameMale45yo);
        verify(this.dataStore, never()).addPlayerToRoster(any(), any());
    }

    // --- Under-age exception: 2 players may be 1 year younger in
    // Masters/Grandmasters/Greatgrand. MIXED treats MALE/NA and FEMALE as
    // fixed slots; DIVERSE fits whichever slot has room.

    private DfvMvName registerUnderAgePlayer(int dfvNumber, LocalDate birthDate, Gender gender) {
        DfvMvName name = mock(DfvMvName.class);
        when(name.getDfvNumber()).thenReturn(dfvNumber);
        when(name.isDse()).thenReturn(Boolean.TRUE);
        DfvPlayer player = mock(DfvPlayer.class);
        when(player.getBirthDate()).thenReturn(birthDate);
        when(player.getGender()).thenReturn(gender);
        when(player.isEligible()).thenReturn(Boolean.TRUE);
        when(this.dataStore.getDfvMvName(dfvNumber)).thenReturn(name);
        when(this.dataStore.getPlayerByDfvNumber(dfvNumber)).thenReturn(player);
        return name;
    }

    private RosterPlayer rosterPlayerWith(LocalDate birthDate, Gender gender) {
        DfvPlayer player = mock(DfvPlayer.class);
        when(player.getBirthDate()).thenReturn(birthDate);
        when(player.getGender()).thenReturn(gender);
        RosterPlayer rp = mock(RosterPlayer.class);
        when(rp.getPlayer()).thenReturn(player);
        return rp;
    }

    private Roster stubMastersRoster(int rosterId, DivisionType type) {
        Roster r = mock(Roster.class);
        when(r.getId()).thenReturn(rosterId);
        when(r.getSeason()).thenReturn(this.season);
        when(r.getDivisionType()).thenReturn(type);
        when(r.getDivisionAge()).thenReturn(DivisionAge.MASTERS);
        when(r.getTeam()).thenReturn(this.teamA);
        when(this.dataStore.get(eq(rosterId), eq(Roster.class))).thenReturn(r);
        return r;
    }

    @Test
    public void testOneYearYoungerMaleAllowedInMastersOpen() throws Exception {
        // 32yo male in the 2016 season — 1 year under the Masters minimum of 33.
        DfvMvName name = registerUnderAgePlayer(9001, LocalDate.of(1984, 6, 1), Gender.MALE);
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_MASTER, name);
        verify(this.dataStore).addPlayerToRoster(eq(this.rosterMaster), any(DfvPlayer.class));
    }

    @Test
    public void testThirdOneYearYoungerRejectedInMastersOpen() throws Exception {
        RosterPlayer rp1 = rosterPlayerWith(LocalDate.of(1984, 3, 1), Gender.MALE);
        RosterPlayer rp2 = rosterPlayerWith(LocalDate.of(1984, 8, 1), Gender.MALE);
        when(this.rosterMaster.getPlayers()).thenReturn(Arrays.asList(rp1, rp2));
        DfvMvName name = registerUnderAgePlayer(9002, LocalDate.of(1984, 11, 1), Gender.MALE);
        this.expected.expect(WebApplicationException.class);
        this.expected.expectMessage("e109");
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_MASTER, name);
        verify(this.dataStore, never()).addPlayerToRoster(any(), any());
    }

    @Test
    public void testTwoYearsYoungerRejectedInMasters() throws Exception {
        // 31yo male — outside the 1-year tolerance.
        DfvMvName name = registerUnderAgePlayer(9003, LocalDate.of(1985, 6, 1), Gender.MALE);
        this.expected.expect(WebApplicationException.class);
        this.expected.expectMessage("age does not match");
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_MASTER, name);
        verify(this.dataStore, never()).addPlayerToRoster(any(), any());
    }

    @Test
    public void testTwentyNineYearOldWomanAllowedInMastersOpen() throws Exception {
        // 29yo woman — with the +3 masters-tier bonus, her effective age is 32,
        // which is 1 year under the 33 threshold. So she fits the exception.
        DfvMvName name = registerUnderAgePlayer(9004, LocalDate.of(1987, 5, 1), Gender.FEMALE);
        this.resource.addPlayerToRoster(this.currentUser, ROSTER_ID_MASTER, name);
        verify(this.dataStore).addPlayerToRoster(eq(this.rosterMaster), any(DfvPlayer.class));
    }

    @Test
    public void testMastersMixedAllowsMalePlusFemaleUnderAge() throws Exception {
        final int rosterId = 9100;
        Roster mixedMasters = stubMastersRoster(rosterId, DivisionType.MIXED);
        RosterPlayer existing = rosterPlayerWith(LocalDate.of(1984, 2, 1), Gender.MALE);
        when(mixedMasters.getPlayers()).thenReturn(Collections.singletonList(existing));

        // 29yo woman fits the still-empty female slot
        DfvMvName name = registerUnderAgePlayer(9005, LocalDate.of(1987, 9, 1), Gender.FEMALE);
        this.resource.addPlayerToRoster(this.currentUser, rosterId, name);
        verify(this.dataStore).addPlayerToRoster(eq(mixedMasters), any(DfvPlayer.class));
    }

    @Test
    public void testMastersMixedRejectsSecondUnderAgeMale() throws Exception {
        final int rosterId = 9101;
        Roster mixedMasters = stubMastersRoster(rosterId, DivisionType.MIXED);
        RosterPlayer existing = rosterPlayerWith(LocalDate.of(1984, 2, 1), Gender.MALE);
        when(mixedMasters.getPlayers()).thenReturn(Collections.singletonList(existing));

        DfvMvName name = registerUnderAgePlayer(9006, LocalDate.of(1984, 7, 1), Gender.MALE);
        this.expected.expect(WebApplicationException.class);
        this.expected.expectMessage("e109");
        this.resource.addPlayerToRoster(this.currentUser, rosterId, name);
        verify(this.dataStore, never()).addPlayerToRoster(any(), any());
    }

    @Test
    public void testMastersMixedDiverseTakesRemainingSlot() throws Exception {
        // 1 under-age male already on the roster — DIVERSE should flex into
        // the female slot.
        final int rosterId = 9102;
        Roster mixedMasters = stubMastersRoster(rosterId, DivisionType.MIXED);
        RosterPlayer existing = rosterPlayerWith(LocalDate.of(1984, 2, 1), Gender.MALE);
        when(mixedMasters.getPlayers()).thenReturn(Collections.singletonList(existing));

        // 29yo DIVERSE — effective age 32, 1 under the 33 threshold.
        DfvMvName name = registerUnderAgePlayer(9007, LocalDate.of(1987, 4, 1), Gender.DIVERSE);
        this.resource.addPlayerToRoster(this.currentUser, rosterId, name);
        verify(this.dataStore).addPlayerToRoster(eq(mixedMasters), any(DfvPlayer.class));
    }

    @Test
    public void testMastersMixedDiverseRejectedWhenBothSlotsFixed() throws Exception {
        // Both a fixed male and a fixed female under-age player are already
        // on the roster — a DIVERSE under-age player would need a third slot.
        final int rosterId = 9103;
        Roster mixedMasters = stubMastersRoster(rosterId, DivisionType.MIXED);
        RosterPlayer existingMale = rosterPlayerWith(LocalDate.of(1984, 2, 1), Gender.MALE);
        RosterPlayer existingFemale = rosterPlayerWith(LocalDate.of(1987, 5, 1), Gender.FEMALE);
        when(mixedMasters.getPlayers()).thenReturn(Arrays.asList(existingMale, existingFemale));

        DfvMvName name = registerUnderAgePlayer(9008, LocalDate.of(1987, 10, 1), Gender.DIVERSE);
        this.expected.expect(WebApplicationException.class);
        this.expected.expectMessage("e109");
        this.resource.addPlayerToRoster(this.currentUser, rosterId, name);
        verify(this.dataStore, never()).addPlayerToRoster(any(), any());
    }

    @Test
    public void testUnderAgeExceptionAppliesToGrandmasters() throws Exception {
        // 39yo male is 1 year under the Grandmasters threshold of 40.
        final int rosterId = 9104;
        Roster grandmasters = mock(Roster.class);
        when(grandmasters.getId()).thenReturn(rosterId);
        when(grandmasters.getSeason()).thenReturn(this.season);
        when(grandmasters.getDivisionType()).thenReturn(DivisionType.OPEN);
        when(grandmasters.getDivisionAge()).thenReturn(DivisionAge.GRANDMASTERS);
        when(grandmasters.getTeam()).thenReturn(this.teamA);
        when(this.dataStore.get(eq(rosterId), eq(Roster.class))).thenReturn(grandmasters);

        DfvMvName name = registerUnderAgePlayer(9009, LocalDate.of(1977, 4, 1), Gender.MALE);
        this.resource.addPlayerToRoster(this.currentUser, rosterId, name);
        verify(this.dataStore).addPlayerToRoster(eq(grandmasters), any(DfvPlayer.class));
    }
}
