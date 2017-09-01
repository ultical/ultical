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
package de.ultical.backend.data;

import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.apache.ibatis.session.SqlSession;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.data.mapper.DfvMvNameMapper;
import de.ultical.backend.data.mapper.DfvPlayerMapper;
import de.ultical.backend.model.DfvPlayer;

public class DataStoreTest {

    private DataStore dStore;
    @Mock
    SqlSession sqlSession;
    @Mock
    DfvPlayerMapper playerMapper;
    @Mock
    DfvMvNameMapper nameMapper;
    final int dfvNrOne = 123;
    final int dfvNrTwo = 234;
    final int dfvNrThree = 345;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(this.sqlSession.getMapper(DfvPlayerMapper.class)).thenReturn(this.playerMapper);
        when(this.sqlSession.getMapper(DfvMvNameMapper.class)).thenReturn(this.nameMapper);

        when(this.playerMapper.getAll()).thenReturn(Arrays.asList(this.buildPlayer("Foo", "Bar", this.dfvNrOne, true),
                this.buildPlayer("Foo", "Bar", this.dfvNrTwo, true),
                this.buildPlayer("Foo", "Bar", this.dfvNrThree, true)));
        when(this.nameMapper.get(this.dfvNrOne)).thenReturn(this.buildName("Foo", "Bar", this.dfvNrOne, true));
        when(this.nameMapper.get(this.dfvNrTwo)).thenReturn(this.buildName("Foo", "Bàr", this.dfvNrTwo, true));
        when(this.nameMapper.get(this.dfvNrThree)).thenReturn(this.buildName("Foo", "Bar", this.dfvNrThree, false));
        this.dStore = new DataStore();
        this.dStore.sqlSession = this.sqlSession;
    }

    private DfvPlayer buildPlayer(String fn, String ln, int dfvNr, boolean eligible) {
        DfvPlayer player = new DfvPlayer();
        player.setFirstName(fn);
        player.setLastName(ln);
        player.setDfvNumber(dfvNr);
        if (eligible) {
            player.setEligibleUntil(null);
        } else {
            player.setEligibleUntil(LocalDateTime.now().minusMonths(1));
        }
        return player;
    }

    private DfvMvName buildName(String fn, String ln, int dfvNr, boolean active) {
        DfvMvName result = new DfvMvName();
        result.setFirstName(fn);
        result.setLastName(ln);
        result.setDfvnr(dfvNr);
        result.setActive(active);
        return result;
    }

}
