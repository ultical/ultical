package de.ultical.backend.jobs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.app.DfvApiConfig;
import de.ultical.backend.app.MailClient;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.app.UltiCalConfig.JobsConfig;
import de.ultical.backend.app.mail.SystemMessage;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.DivisionAge;
import de.ultical.backend.model.DivisionType;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.Season;
import de.ultical.backend.model.Surface;
import de.ultical.backend.model.Team;
import de.ultical.backend.model.User;

public class DfvProfileLoaderTest {

    private static final String TOKEN = "abcdEFGhiJKlm";
    private static final String SECRET = "very secret";
    private static final String TARGET_URL = "https://dfv.profile.loader.test/apiConfig";
    @Mock
    Client client;
    @Mock
    Client exceptionThrowingClient;
    @Mock
    UltiCalConfig config;
    @Mock
    DataStore dataStore;
    @Mock
    WebTarget target;
    @Mock
    Invocation.Builder builder;
    @Mock
    JobsConfig jobs;
    @Mock
    DfvApiConfig apiConfig;
    @Mock
    UltiCalConfig noJobsConfig;
    @Mock
    AutoCloseable closable;
    @Mock
    MailClient mailClient;

    private List<DfvMvName> responseList = Collections.emptyList();

    private DfvProfileLoader profileLoader;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(this.config.getJobs()).thenReturn(this.jobs);
        when(this.jobs.isDfvMvSyncEnabled()).thenReturn(Boolean.TRUE);
        when(this.config.getDfvApi()).thenReturn(this.apiConfig);
        when(this.apiConfig.getSecret()).thenReturn(SECRET);
        when(this.apiConfig.getToken()).thenReturn(TOKEN);
        when(this.apiConfig.getUrl()).thenReturn(TARGET_URL);
        when(this.client.target(Mockito.anyString())).thenReturn(this.target);
        when(this.target.queryParam(Mockito.anyString(), Mockito.any())).thenReturn(this.target);
        when(this.target.path(Mockito.anyString())).thenReturn(this.target);
        when(this.target.request(Mockito.anyString())).thenReturn(this.builder);
        when(this.builder.get(Mockito.<GenericType<List<DfvMvName>>> any())).thenReturn(this.responseList);
        when(this.dataStore.getClosable()).thenReturn(this.closable);

        WebTarget exceptionThrowingTarget = Mockito.mock(WebTarget.class);
        when(this.exceptionThrowingClient.target(Mockito.anyString())).thenReturn(exceptionThrowingTarget);
        when(exceptionThrowingTarget.queryParam(Mockito.anyString(), Mockito.any()))
                .thenReturn(exceptionThrowingTarget);
        when(exceptionThrowingTarget.path(Mockito.anyString())).thenReturn(exceptionThrowingTarget);
        Invocation.Builder exceptionThrowingBuilder = Mockito.mock(Invocation.Builder.class);
        when(exceptionThrowingTarget.request(Mockito.anyString())).thenReturn(exceptionThrowingBuilder);
        when(exceptionThrowingBuilder.get(Mockito.<GenericType<List<DfvMvName>>> any()))
                .thenThrow(WebApplicationException.class);

        JobsConfig noDfvSync = Mockito.mock(JobsConfig.class);
        when(noDfvSync.isDfvMvSyncEnabled()).thenReturn(Boolean.FALSE);
        when(this.noJobsConfig.getJobs()).thenReturn(noDfvSync);

        Season season = new Season();
        season.setPlusOneYear(false);
        season.setYear(2016);
        season.setSurface(Surface.TURF);

        User admin = new User();
        admin.setEmail("test@example.com");
        admin.setEmailConfirmed(true);
        DfvPlayer adminPlayer = new DfvPlayer();
        adminPlayer.setFirstName("Test");
        adminPlayer.setLastName("Admin");
        admin.setDfvPlayer(adminPlayer);

        Team team = new Team();
        team.setName("dfdf");
        team.setAdmins(Collections.singletonList(admin));

        Roster roster = new Roster();
        roster.setDivisionAge(DivisionAge.REGULAR);
        roster.setDivisionType(DivisionType.OPEN);
        roster.setSeason(season);
        roster.setTeam(team);
        roster.setNameAddition("einer muss gehen");
        roster.setId(1111);

        DfvPlayer updatedPlayer = new DfvPlayer();
        updatedPlayer.setDfvNumber(123456);
        updatedPlayer.setFirstName("Brodie");
        updatedPlayer.setLastName("Smith");
        updatedPlayer.setId(42);

        when(this.dataStore.getRosterForPlayer(updatedPlayer)).thenReturn(Collections.singletonList(roster));
        when(this.dataStore.getPlayersToUpdate()).thenReturn(Collections.singletonList(updatedPlayer));
        /*
         * we return a list of blocking dates that are all in the future.
         */
        when(this.dataStore.getRosterBlockingDates(roster.getId())).thenReturn(Arrays.<LocalDate> asList(
                LocalDate.now().plusDays(1), LocalDate.now().plusMonths(1), LocalDate.now().plusWeeks(3)));

        this.profileLoader = new DfvProfileLoader();
        this.profileLoader.client = this.client;
        this.profileLoader.config = this.config;
        this.profileLoader.dataStore = this.dataStore;
        this.profileLoader.mailClient = this.mailClient;
    }

    @Test
    public void testNoSync() throws Exception {
        this.profileLoader.config = this.noJobsConfig;
        assertFalse(this.profileLoader.getDfvMvNames());
        verify(this.dataStore, never()).getClosable();
    }

    @Test
    public void testNormalRun() throws Exception {
        assertTrue(this.profileLoader.getDfvMvNames());
        verify(this.closable).close();
        verify(this.dataStore).getClosable();
        verify(this.client, times(2)).target(eq(TARGET_URL));
        verify(this.target, times(2)).queryParam(eq("secret"), eq(SECRET));
        verify(this.target, times(2)).queryParam(eq("token"), eq(TOKEN));
        verify(this.target, times(1)).path("profile/sparte/ultimate");
        verify(this.target, times(2)).request(MediaType.APPLICATION_JSON);
        verify(this.builder).get(any(GenericType.class));
        verify(this.dataStore).refreshDfvNames(this.responseList);

        /*
         * verify the update part: Player is removed from roster and mail to
         * admins is sent
         */
        verify(this.mailClient).sendMail(any(SystemMessage.class));
    }

    @Test
    public void testPlayerNotRemoved() throws Exception {
        when(this.dataStore.getRosterBlockingDates(1111))
                .thenReturn(Arrays.asList(LocalDate.now().plusDays(4), LocalDate.now().minusDays(1)));
        assertTrue(this.profileLoader.getDfvMvNames());
        /*
         * Player must not be removed from roster as one blocking-date lies in
         * the past.
         */
        verify(this.dataStore, never()).removePlayerFromRoster(42, 1111);
        verify(this.mailClient).sendMail(any(SystemMessage.class));
    }

    @Test
    public void testPlayerNotRemovedSeasonOver() throws Exception {
        when(this.dataStore.getRosterBlockingDates(1111))
                .thenReturn(Arrays.asList(LocalDate.now().minusDays(1), LocalDate.now().minusMonths(2)));
        assertTrue(this.profileLoader.getDfvMvNames());
        /*
         * in this case nothing is done by the profile loader, only the event is
         * logged.
         */
        verify(this.dataStore, never()).removePlayerFromRoster(42, 1111);
        verify(this.mailClient).sendMail(any(SystemMessage.class));
    }

    @Test
    public void testExceptionRun() throws Exception {
        this.profileLoader.client = this.exceptionThrowingClient;
        try {
            this.profileLoader.getDfvMvNames();
            Assert.fail("mocking is wrong. This part of code shoudl be unreachable");
        } catch (WebApplicationException wae) {

        }
        verify(this.closable).close();
        verify(this.dataStore).getClosable();
        verify(this.dataStore, Mockito.never()).refreshDfvNames(Mockito.anyList());
    }

}
