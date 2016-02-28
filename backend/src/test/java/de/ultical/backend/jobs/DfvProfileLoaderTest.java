package de.ultical.backend.jobs;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.app.UltiCalConfig.JobsConfig;
import de.ultical.backend.data.DataStore;

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

        this.profileLoader = new DfvProfileLoader();
        this.profileLoader.client = this.client;
        this.profileLoader.config = this.config;
        this.profileLoader.dataStore = this.dataStore;
    }

    @Test
    public void testNoSync() throws Exception {
        this.profileLoader.config = this.noJobsConfig;
        assertFalse(this.profileLoader.getDfvMvNames());
        verify(this.dataStore, never()).getClosable();
    }

    @Test
    public void testNormalRun() throws Exception {
        Assert.assertTrue(this.profileLoader.getDfvMvNames());
        verify(this.closable).close();
        verify(this.dataStore).getClosable();
        verify(this.client).target(eq(TARGET_URL));
        verify(this.target).queryParam(eq("secret"), eq(SECRET));
        verify(this.target).queryParam(eq("token"), eq(TOKEN));
        verify(this.target).path("profile");
        verify(this.target).request(MediaType.APPLICATION_JSON);
        verify(this.builder).get(Mockito.any(GenericType.class));
        verify(this.dataStore).refreshDfvNames(this.responseList);
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
