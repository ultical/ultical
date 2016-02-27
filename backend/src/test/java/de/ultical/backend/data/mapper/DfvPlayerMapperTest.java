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
package de.ultical.backend.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
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
