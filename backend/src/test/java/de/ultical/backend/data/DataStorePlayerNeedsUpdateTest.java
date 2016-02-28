package de.ultical.backend.data;

import static de.ultical.backend.data.DataStore.PlayerNeedsUpdatePredicate.needsUpdate;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.data.DataStore.PlayerMvNamePair;
import de.ultical.backend.model.DfvPlayer;

public class DataStorePlayerNeedsUpdateTest {

    private static final int DFV_NUMMER = 1234567;
    private static final String BAR = "bar";
    private static final String FOO = "Foo";
    private PlayerMvNamePair pair1, pair2, pair3, pair4, pair5, pair6, pair7, pair8;

    @Before
    public void setUp() throws Exception {
        DfvMvName firstName = this.buildName(FOO, BAR, DFV_NUMMER, true);
        DfvPlayer firstPlayer = this.buildPlayer(FOO, BAR, DFV_NUMMER, true);
        this.pair1 = new PlayerMvNamePair(firstPlayer, firstName);

        DfvPlayer secondPlayer = this.buildPlayer(FOO, BAR, 1234567, false);
        this.pair2 = new PlayerMvNamePair(secondPlayer, firstName);

        this.pair3 = new PlayerMvNamePair(this.buildPlayer("Footur", BAR, DFV_NUMMER, true), firstName);
        this.pair4 = new PlayerMvNamePair(this.buildPlayer(null, BAR, DFV_NUMMER, true), firstName);

        this.pair5 = new PlayerMvNamePair(this.buildPlayer(null, BAR, DFV_NUMMER, true),
                this.buildName(null, BAR, DFV_NUMMER, true));
        this.pair6 = new PlayerMvNamePair(this.buildPlayer(FOO, "barbara", DFV_NUMMER, true), firstName);
        this.pair7 = new PlayerMvNamePair(this.buildPlayer(FOO, null, DFV_NUMMER, true), firstName);
        this.pair8 = new PlayerMvNamePair(this.buildPlayer(FOO, null, DFV_NUMMER, true),
                this.buildName(FOO, null, DFV_NUMMER, true));
    }

    private DfvMvName buildName(String fn, String ln, int dfvNr, boolean act) {
        DfvMvName firstName = new DfvMvName();
        firstName.setActive(act);
        firstName.setFirstName(fn);
        firstName.setLastName(ln);
        firstName.setDfvnr(dfvNr);
        return firstName;
    }

    private DfvPlayer buildPlayer(String fn, String ln, int dfvNr, boolean active) {
        DfvPlayer firstPlayer = new DfvPlayer();
        firstPlayer.setActive(active);
        firstPlayer.setFirstName(fn);
        firstPlayer.setLastName(ln);
        firstPlayer.setDfvNumber(dfvNr);
        return firstPlayer;
    }

    @Test
    public void testEverythingSameNonNull() {
        assertFalse(needsUpdate(this.pair1));
    }

    @Test
    public void testDifferenceActive() {
        assertTrue(needsUpdate(this.pair2));
    }

    @Test
    public void testFirstNameDiffer() {
        assertTrue(needsUpdate(this.pair3));
    }

    @Test
    public void testFirstNameDifferNull() {
        assertTrue(needsUpdate(this.pair4));
    }

    @Test
    public void testFirstNameNull() {
        assertFalse(needsUpdate(this.pair5));
    }

    @Test
    public void testLastNameDifferNonNull() {
        assertTrue(needsUpdate(this.pair6));
    }

    @Test
    public void testLastNameDifferNull() {
        assertTrue(needsUpdate(this.pair7));
    }

    @Test
    public void testLastNameNull() {
        assertFalse(needsUpdate(this.pair8));
    }
}
