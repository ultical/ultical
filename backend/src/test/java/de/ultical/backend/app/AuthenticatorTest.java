package de.ultical.backend.app;

import org.junit.Test;
import org.junit.Assert;
import org.junit.rules.ExpectedException;
import org.junit.Rule;

import de.ultical.backend.exception.AuthorizationException;
import de.ultical.backend.model.User;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;



public class AuthenticatorTest {

    @Mock User unauthorizedUser;
    @Mock User authorizedUser;

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);

	Mockito.when(this.unauthorizedUser.getEmail()).thenReturn("unauthorized@ultical.com");
	Mockito.when(this.unauthorizedUser.getId()).thenReturn(13);
	Mockito.when(this.authorizedUser.getEmail()).thenReturn("authorized@ultical.com");
	Mockito.when(this.authorizedUser.getId()).thenReturn(42);

	Authenticator.addAdmin("authorized@ultical.com");
    }
    
    @Rule
    public ExpectedException expected = ExpectedException.none();
    
    @Test
    public void testAddAdmins() throws Exception {
	Assert.assertFalse(Authenticator.addAdmin(null));
	Assert.assertTrue(Authenticator.addAdmin("admin"));
	Assert.assertFalse(Authenticator.addAdmin("admin"));
    }

    @Test
    public void testOverallAdminUserNull() throws Exception {
	expected.expect(NullPointerException.class);
	Authenticator.assureOverallAdmin(null);
    }

    @Test
    public void testOverallAdminUserUnauthorized() throws Exception {
	expected.expect(AuthorizationException.class);
	expected.expectMessage("User unauthorized@ultical.com (id=13) is not authorized as overall admin");
	Authenticator.assureOverallAdmin(this.unauthorizedUser);
    }

    @Test
    public void testOverallAdminUserAuthorized() throws Exception {
	Authenticator.assureOverallAdmin(this.authorizedUser);
    }
    
}
