/**
 * Copyright (C) 2015-2016 ultical contributors
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 * * Apache License, version 2.0
 * * Apache Software License, version 1.0
 * * Mozilla Public License, versions 1.0, 1.1 and 2.0
 * * Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU Affero General Public License
 * version 3 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the  GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
