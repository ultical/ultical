package de.ultical.backend.api;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

import java.util.Collections;

import javax.ws.rs.WebApplicationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.DivisionRegistration.DivisionRegistrationStatus;
import de.ultical.backend.model.Event;
import de.ultical.backend.model.TeamRegistration;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.TournamentFormat;
import de.ultical.backend.model.User;

/**
 * Verifies that changing a registered team's status is reserved for admins of the tournament format, while event
 * ("tournament") admins keep managing the remaining registration fields.
 */
public class TournamentResourceTest {

    private final static int TEAM_REG_ID = 100;
    private final static int EVENT_ID = 1;
    private final static int EDITION_ID = 50;
    private final static int FORMAT_ADMIN_ID = 10;
    private final static int EVENT_ADMIN_ID = 20;

    private TournamentResource resource;

    @Mock
    private DataStore ds;
    @Mock
    private TournamentEdition edition;
    @Mock
    private TournamentFormat format;
    @Mock
    private Event event;
    @Mock
    private User formatAdmin;
    @Mock
    private User eventAdmin;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.resource = new TournamentResource();
        this.resource.dataStore = this.ds;

        when(this.ds.getClosable()).thenReturn(mock(DataStore.DataStoreCloseable.class));
        when(this.ds.getEditionByTeamRegistration(TEAM_REG_ID)).thenReturn(this.edition);
        when(this.ds.getTeamRegistrationStatus(TEAM_REG_ID)).thenReturn(DivisionRegistrationStatus.PENDING);

        when(this.edition.getId()).thenReturn(EDITION_ID);
        when(this.edition.isAllowEventTeamRegManagement()).thenReturn(true);
        when(this.edition.getTournamentFormat()).thenReturn(this.format);

        when(this.ds.getFormatByEdition(EDITION_ID)).thenReturn(this.format);
        when(this.format.getAdmins()).thenReturn(Collections.singletonList(this.formatAdmin));

        when(this.ds.get(EVENT_ID, Event.class)).thenReturn(this.event);
        when(this.event.getId()).thenReturn(EVENT_ID);
        when(this.event.getAdmins()).thenReturn(Collections.singletonList(this.eventAdmin));
        when(this.event.getTournamentEdition()).thenReturn(this.edition);

        when(this.formatAdmin.getId()).thenReturn(FORMAT_ADMIN_ID);
        when(this.eventAdmin.getId()).thenReturn(EVENT_ADMIN_ID);
    }

    private TeamRegistration registration(DivisionRegistrationStatus status) {
        TeamRegistration reg = new TeamRegistration();
        reg.setId(TEAM_REG_ID);
        reg.setStatus(status);
        return reg;
    }

    @Test(expected = WebApplicationException.class)
    public void testEventAdminCannotChangeStatus() throws Exception {
        // stored status is PENDING; submitting CONFIRMED is a status change -> requires format admin
        this.resource.updateTeamRegistration(EVENT_ID, this.registration(DivisionRegistrationStatus.CONFIRMED),
                this.eventAdmin);
    }

    @Test
    public void testEventAdminCanUpdateNonStatusFields() throws Exception {
        // unchanged status -> event admin may still update the registration
        boolean result = this.resource.updateTeamRegistration(EVENT_ID,
                this.registration(DivisionRegistrationStatus.PENDING), this.eventAdmin);
        Assert.assertTrue(result);
        verify(this.ds).update(any(TeamRegistration.class));
    }

    @Test
    public void testFormatAdminCanChangeStatusViaEventPath() throws Exception {
        boolean result = this.resource.updateTeamRegistration(EVENT_ID,
                this.registration(DivisionRegistrationStatus.CONFIRMED), this.formatAdmin);
        Assert.assertTrue(result);
        verify(this.ds).update(any(TeamRegistration.class));
    }

    @Test
    public void testFormatAdminCanChangeStatusViaEditionPath() throws Exception {
        boolean result = this.resource.updateTeamRegistration(0,
                this.registration(DivisionRegistrationStatus.CONFIRMED), this.formatAdmin);
        Assert.assertTrue(result);
        verify(this.ds).update(any(TeamRegistration.class));
    }

    @Test(expected = WebApplicationException.class)
    public void testEventAdminBlockedWhenManagementDisabled() throws Exception {
        // even without a status change, event admins lose access when event-level management is disabled
        when(this.edition.isAllowEventTeamRegManagement()).thenReturn(false);
        this.resource.updateTeamRegistration(EVENT_ID, this.registration(DivisionRegistrationStatus.PENDING),
                this.eventAdmin);
    }

    @Test(expected = WebApplicationException.class)
    public void testEventAdminCannotChangeStatusBatch() throws Exception {
        this.resource.updateTeamRegistrations(EVENT_ID,
                Collections.singletonList(this.registration(DivisionRegistrationStatus.DECLINED)), this.eventAdmin);
    }

    @Test
    public void testEventAdminCanBatchUpdateNonStatusFields() throws Exception {
        boolean result = this.resource.updateTeamRegistrations(EVENT_ID,
                Collections.singletonList(this.registration(DivisionRegistrationStatus.PENDING)), this.eventAdmin);
        Assert.assertTrue(result);
        verify(this.ds).updateAll(anyList());
    }

    @Test
    public void testFormatAdminCanChangeStatusBatch() throws Exception {
        boolean result = this.resource.updateTeamRegistrations(EVENT_ID,
                Collections.singletonList(this.registration(DivisionRegistrationStatus.CONFIRMED)), this.formatAdmin);
        Assert.assertTrue(result);
        verify(this.ds).updateAll(anyList());
    }
}
