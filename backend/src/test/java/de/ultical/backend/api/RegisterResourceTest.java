package de.ultical.backend.api;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.api.transferClasses.DfvMvPlayer;
import de.ultical.backend.api.transferClasses.RegisterRequest;
import de.ultical.backend.api.transferClasses.RegisterResponse;
import de.ultical.backend.api.transferClasses.RegisterResponse.RegisterResponseStatus;
import de.ultical.backend.app.DfvApiConfig;
import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.User;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import javax.ws.rs.client.Client; 
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class RegisterResourceTest {

    private RegisterResource resource;
    @Mock
    private DataStore ds;
    @Mock
    private Client client;
    @Mock
    private UltiCalConfig config;
    @Mock
    private DfvApiConfig dfvConfig;
    @Mock
    private DfvMvName dfvName;
    @Mock
    private DfvPlayer dfvPlayer;
    
    private Date birthDate;
    
    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);

	Calendar cal = Calendar.getInstance();
	cal.set(Calendar.YEAR, 1980);
	cal.set(Calendar.MONTH, 0);
	cal.set(Calendar.DAY_OF_MONTH, 1);
	this.birthDate = cal.getTime();

	this.resource = new RegisterResource();
	
	when(this.config.getDfvApi()).thenReturn(this.dfvConfig);
	when(this.dfvConfig.getSecret()).thenReturn("secret");
	when(this.dfvConfig.getToken()).thenReturn("token");
	when(this.dfvConfig.getUrl()).thenReturn("url");
	this.resource.config = this.config;
		
	this.resource.dataStore = this.ds;
	when(this.ds.getClosable()).thenReturn(mock(AutoCloseable.class));
	when(this.ds.getDfvNames(eq("test"),eq("User"))).thenReturn(Collections.emptyList());
	when(this.ds.getDfvNames(eq("known"),eq("User"))).thenReturn(Collections.singletonList(this.dfvName));
	this.resource.client = this.client;
	WebTarget target = mock(WebTarget.class);
	when(this.client.target(any(String.class))).thenReturn(target);
	when(target.path(any(String.class))).thenReturn(target);
	when(target.queryParam(any(String.class),any(String.class))).thenReturn(target);
	Invocation.Builder builder = mock(Invocation.Builder.class);
	when(target.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
	DfvMvPlayer mvPlayer = mock(DfvMvPlayer.class);
	when(builder.get(eq(DfvMvPlayer.class))).thenReturn(mvPlayer);
	
	when(this.dfvName.getDfvNumber()).thenReturn(12345);

	when(mvPlayer.getDobString()).thenReturn("1980-01-01");
	when(mvPlayer.getEmail()).thenReturn("registered@ultical.com");
	
    }

    @Test
    public void testRegisterPasswordTooShort() throws Exception {
	RegisterRequest req = mock(RegisterRequest.class);
	when(req.getPassword()).thenReturn("tooShort");
	RegisterResponse response = this.resource.registerRequest(req);
	assertNotNull(response);
	assertEquals(response.getStatus(), RegisterResponseStatus.VALIDATION_ERROR);
    }

    @Test
    public void testRegisterUnknownPlayer() throws Exception {
	RegisterRequest req = mock(RegisterRequest.class);
	when(req.getPassword()).thenReturn("PasswordLongEnough");
	when(req.getFirstName()).thenReturn("test");
	when(req.getLastName()).thenReturn("User");
	when(req.getBirthDate()).thenReturn(this.birthDate);
	RegisterResponse response = this.resource.registerRequest(req);
	assertNotNull(response);
	assertEquals(response.getStatus(), RegisterResponseStatus.NOT_FOUND);
	verify(this.ds, never()).storeUser(any(User.class),any(Boolean.class));
    }

    @Test
    public void testRegisterKnownUser() throws Exception {
	RegisterRequest req = mock(RegisterRequest.class);
	when(req.getPassword()).thenReturn("PasswordLongEnough");
	when(req.getFirstName()).thenReturn("known");
	when(req.getLastName()).thenReturn("User");
	when(req.getBirthDate()).thenReturn(this.birthDate);
	when(req.getEmail()).thenReturn("registered@ultical.com");
	RegisterResponse response = this.resource.registerRequest(req);
	assertNotNull(response);
	assertEquals(RegisterResponseStatus.SUCCESS, response.status);
    }

    
}
