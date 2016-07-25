package de.ultical.backend.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.Gender;
import de.ultical.backend.model.Player;
import de.ultical.backend.utils.test.PrepareDBRule;

public class DfvPlayerMapperTest {

    private DfvPlayer dfvPlayer;

    @ClassRule
    public static PrepareDBRule dbRule = new PrepareDBRule();

    @Before
    public void setUp() throws Exception {
        dbRule.getSession();
        this.dfvPlayer = new DfvPlayer();
        this.dfvPlayer.setDfvNumber(123456);
        this.dfvPlayer.setFirstName("Brodie");
        this.dfvPlayer.setLastName("Smith");
        this.dfvPlayer.setGender(Gender.MALE);
        this.dfvPlayer.setLastModified(LocalDateTime.now());

        this.dfvPlayer.setBirthDate(LocalDate.now());
    }

    @After
    public void tearDown() throws Exception {
        dbRule.closeSession();
    }

    @Test
    public void test() {
        PlayerMapper playerMapper = dbRule.getSession().getMapper(PlayerMapper.class);
        DfvPlayerMapper mapper = dbRule.getSession().getMapper(DfvPlayerMapper.class);
        this.dfvPlayer.getId();
        playerMapper.insertPlayer(this.dfvPlayer, this.dfvPlayer instanceof DfvPlayer);
        final int insertedId = this.dfvPlayer.getId();
        mapper.insert(this.dfvPlayer);
        dbRule.getSession().commit();
        List<Player> allPlayers = playerMapper.getAll();
        assertNotNull(allPlayers);
        assertEquals(1, allPlayers.size());
        assertTrue(allPlayers.get(0) instanceof DfvPlayer);

        final DfvPlayer foundPlayer = (DfvPlayer) playerMapper.get(insertedId);
        assertNotNull(foundPlayer);
        assertEquals(1, foundPlayer.getVersion());
        assertEquals(insertedId, foundPlayer.getId());
        assertNotNull(foundPlayer.getFirstName());
        assertEquals(this.dfvPlayer.getGender(), foundPlayer.getGender());
        assertEquals(this.dfvPlayer.getBirthDate(), foundPlayer.getBirthDate());
        assertEquals(this.dfvPlayer.getFirstName(), foundPlayer.getFirstName());
        assertEquals(this.dfvPlayer.getLastName(), foundPlayer.getLastName());

        /*
         * test update of players
         */

        Integer shouldBeOne = playerMapper.update(foundPlayer);
        mapper.update(foundPlayer);
        assertEquals(Integer.valueOf(1), shouldBeOne);
        dbRule.getSession().commit();
        DfvPlayer updatedPlayer = (DfvPlayer) playerMapper.get(insertedId);
        this.checkUpdatedPlayer(updatedPlayer);

        // update again, but using the foundPlayer instance, which has the wrong
        // version set by now.
        Integer shouldBeZero = playerMapper.update(foundPlayer);
        assertEquals(Integer.valueOf(0), shouldBeZero);
        // as update is supposed to not succeed, we can check the player using
        // the same checks as after the first update
        updatedPlayer = (DfvPlayer) playerMapper.get(insertedId);
        this.checkUpdatedPlayer(updatedPlayer);
    }

    private void checkUpdatedPlayer(final DfvPlayer updatedPlayer) {
        assertNotNull(updatedPlayer);
        assertEquals(2, updatedPlayer.getVersion());
        assertEquals(this.dfvPlayer.getGender(), updatedPlayer.getGender());
        assertEquals(this.dfvPlayer.getBirthDate(), updatedPlayer.getBirthDate());
        assertEquals(this.dfvPlayer.getFirstName(), updatedPlayer.getFirstName());
        assertEquals(this.dfvPlayer.getLastName(), updatedPlayer.getLastName());
    }

}
