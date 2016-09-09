package de.ultical.backend.data;

import static de.ultical.backend.data.DataStore.PlayerNeedsUpdatePredicate.needsUpdate;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.data.DataStore.PlayerMvNamePair;
import de.ultical.backend.model.DfvPlayer;

public class DataStorePlayerNeedsUpdateTest {

    private static final int DFV_NUMMER = 1234567;
    private static final String BAR = "bar";
    private static final String FOO = "Foo";
    private PlayerMvNamePair pair1, pair2, pair3, pair4;

    @Before
    public void setUp() throws Exception {
        DfvMvName firstName = this.buildName(FOO, BAR, DFV_NUMMER, true, LocalDateTime.now());
        DfvPlayer firstPlayer = this.buildPlayer(FOO, BAR, DFV_NUMMER, true, LocalDateTime.of(2016, 4, 1, 23, 11));
        this.pair1 = new PlayerMvNamePair(firstPlayer, firstName);

        DfvPlayer secondPlayer = this.buildPlayer(FOO, BAR, 1234567, false, null);
        this.pair2 = new PlayerMvNamePair(secondPlayer, firstName);

        this.pair3 = new PlayerMvNamePair(
                this.buildPlayer("Footur", BAR, DFV_NUMMER, true, LocalDateTime.now().plusDays(5)), firstName);

        this.pair4 = new PlayerMvNamePair(this.buildPlayer(FOO, null, DFV_NUMMER, true, LocalDateTime.now()),
                this.buildName(FOO, null, DFV_NUMMER, true, null));
    }

    private DfvMvName buildName(String fn, String ln, int dfvNr, boolean act, LocalDateTime date) {
        DfvMvName firstName = new DfvMvName();
        firstName.setActive(act);
        firstName.setFirstName(fn);
        firstName.setLastName(ln);
        firstName.setDfvnr(dfvNr);
        firstName.setLastModified(date);
        return firstName;
    }

    private DfvPlayer buildPlayer(String fn, String ln, int dfvNr, boolean eligible, LocalDateTime mod) {
        DfvPlayer firstPlayer = new DfvPlayer();
        if (eligible) {
            firstPlayer.setEligibleUntil(null);
        } else {
            firstPlayer.setEligibleUntil(LocalDateTime.now().minusMonths(1));
        }
        firstPlayer.setFirstName(fn);
        firstPlayer.setLastName(ln);
        firstPlayer.setDfvNumber(dfvNr);
        firstPlayer.setLastModified(mod);
        return firstPlayer;
    }

    @Test
    public void testNeedsUpdateNonNull() {
        assertTrue(needsUpdate(this.pair1));
    }

    @Test
    public void testNeedUpdateFalse() {
        assertFalse(needsUpdate(this.pair3));
    }

    @Test
    public void testNeedUpdateNull() {
        assertFalse(needsUpdate(this.pair4));
    }

    @Test(expected = NullPointerException.class)
    public void testNeedUpdatePlayerDateNull() {
        needsUpdate(this.pair2);
    }

}
