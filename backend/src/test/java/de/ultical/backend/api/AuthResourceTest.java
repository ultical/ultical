package de.ultical.backend.api;

import org.junit.*;
import org.mockito.*;

import static org.mockito.Mockito.*;
import javax.ws.rs.WebApplicationException;

import de.ultical.backend.api.transferClasses.AuthResponse;
import de.ultical.backend.api.transferClasses.AuthResponse.AuthResponseStatus;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.User;
import de.ultical.backend.model.DfvPlayer;

public class AuthResourceTest {

    private AuthResource resourceUnderTest;
    private AuthResource resourceWithoutDS;

    private User input;
    private DfvPlayer p;
    private static final String KNOWN_AND_COMPLETELY_REGISTERED_MAIL = "test@ultical.de";
    private static final String UNCONFIRMED_EMAIL = "unconfirmed@ultical.de";
    private static final String NO_OPT_IN_EMAIL = "no_opt_in@ultical.de";
    private static final String HASHED_TEST_PASSWORD = "$2a$10$QdZ62bMi4Lktyj0gz3cayOMZmWEm4vq3bm4rRz5/T9/amD3tgI/nW";
    
    @Mock
    private DataStore ds;
    @Mock
    private User registeredUser;
    @Mock
    private User unconfiremdUser;
    @Mock
    private User notOptedInUser;
    
    @Before
    public void setUp() {
	MockitoAnnotations.initMocks(this);
	this.resourceUnderTest = new AuthResource();
	this.resourceWithoutDS = new AuthResource();
	this.resourceUnderTest.dataStore = ds;
	this.input = new User();

	/*
	 * some necessary mocking
	 */
	when(this.ds.getClosable()).thenReturn(Mockito.mock(AutoCloseable.class));
	when(this.ds.getUserByEmail(KNOWN_AND_COMPLETELY_REGISTERED_MAIL)).thenReturn(this.registeredUser);
	when(this.registeredUser.getPassword()).thenReturn(HASHED_TEST_PASSWORD);
	when(this.registeredUser.isEmailConfirmed()).thenReturn(true);
	when(this.registeredUser.isDfvEmailOptIn()).thenReturn(true);
	when(this.ds.getUserByEmail(UNCONFIRMED_EMAIL)).thenReturn(this.unconfiremdUser);
	when(this.unconfiremdUser.getPassword()).thenReturn(HASHED_TEST_PASSWORD);
	when(this.ds.getUserByEmail(NO_OPT_IN_EMAIL)).thenReturn(this.notOptedInUser);
	when(this.notOptedInUser.getPassword()).thenReturn(HASHED_TEST_PASSWORD);
	when(this.notOptedInUser.isEmailConfirmed()).thenReturn(true);
    }

    @Test
    public void testRequestNull() throws Exception {
	Assert.assertNull(this.resourceUnderTest.AuthRequest(null));
    }

    @Test(expected = WebApplicationException.class)
    public void testNoDataStore() throws Exception {
	this.resourceWithoutDS.AuthRequest(Mockito.mock(User.class));
    }

    /**
     * I assume the scenario tested here is very unlikely to occur in
     * 'real life' as the frontend will disable the submit-button if
     * there is no email, but well the API is public...
     */
    @Test
    public void testUserMailIsNull() throws Exception {
	this.resourceUnderTest.AuthRequest(input);
    }
    
    @Test
    public void testHappyPath() throws Exception {
	input.setEmail(KNOWN_AND_COMPLETELY_REGISTERED_MAIL);
	input.setPassword("test");
	AuthResponse response = this.resourceUnderTest.AuthRequest(input);
	Assert.assertNotNull(response);
	Assert.assertEquals(response.status, AuthResponseStatus.SUCCESS );
	Assert.assertNotNull(response.user);
	Assert.assertEquals(response.user.getPassword(), HASHED_TEST_PASSWORD);
    }

    @Test
    public void testUnknownEmail() throws Exception {
	input.setEmail("völlig@unbekannt.de");
	AuthResponse response = this.resourceUnderTest.AuthRequest(input);
	Assert.assertNotNull(response);
	Assert.assertEquals(response.status, AuthResponseStatus.WRONG_CREDENTIALS);
    }

    @Test
    public void testWrongPassword() throws Exception {
	input.setEmail(KNOWN_AND_COMPLETELY_REGISTERED_MAIL);
	input.setPassword("tset");
	AuthResponse response = this.resourceUnderTest.AuthRequest(input);
	Assert.assertNotNull(response);
	Assert.assertEquals(response.status, AuthResponseStatus.WRONG_CREDENTIALS);
	Assert.assertNull(response.user);
    }

    @Test
    public void testEmailNotConfirmed() throws Exception {
	this.input.setEmail(UNCONFIRMED_EMAIL);
	this.input.setPassword("test");
	AuthResponse response = this.resourceUnderTest.AuthRequest(input);
	Assert.assertNotNull(response);
	Assert.assertNull(response.user);
	Assert.assertEquals(response.status, AuthResponseStatus.EMAIL_NOT_CONFIRMED);
    }

    @Test
    public void testNoDfvOptIn() throws Exception {
	this.input.setEmail(NO_OPT_IN_EMAIL);
	this.input.setPassword("test");
	AuthResponse response = this.resourceUnderTest.AuthRequest(input);
	Assert.assertNotNull(response);
	Assert.assertNull(response.user);
	Assert.assertEquals(response.status, AuthResponseStatus.DFV_EMAIL_NOT_OPT_IN);
    }
}
