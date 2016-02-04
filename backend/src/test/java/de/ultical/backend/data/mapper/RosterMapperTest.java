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
import static org.junit.Assert.assertNull;

import java.util.List;

import org.apache.ibatis.exceptions.PersistenceException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.utils.test.PrepareDBRule;

public class RosterMapperTest {

    private static final String NACHNAME = "Dfv-Mv Nachname";

    private static final String VORNAME = "Dfv-Mv Vorname";

    @ClassRule
    public static PrepareDBRule DBRULE = new PrepareDBRule();

    private DfvMvName dfvMvName;
    private DfvMvNameMapper mapper;

    @Before
    public void setUp() throws Exception {
        this.dfvMvName = new DfvMvName();
        this.dfvMvName.setDfvnr(3234567);
        this.dfvMvName.setVorname(VORNAME);
        this.dfvMvName.setNachname(NACHNAME);
        this.dfvMvName.setDse(true);

        this.mapper = DBRULE.getSession().getMapper(DfvMvNameMapper.class);
    }

    @Test
    public void test() {
        Integer id = this.mapper.insert(this.dfvMvName);
        assertNotNull(id);
        DfvMvName found = this.mapper.get(3234567);
        assertNotNull(found);
        assertEquals(VORNAME, found.getFirstName());
        assertEquals(NACHNAME, found.getLastName());
        assertEquals(true, found.isDse());

        DfvMvName other = new DfvMvName();
        other.setDfvnr(41234568);
        other.setFirstName(VORNAME);
        other.setLastName(NACHNAME);
        this.mapper.insert(other);

        other = new DfvMvName();
        other.setDfvnr(51234569);
        other.setVorname(VORNAME);
        other.setNachname(NACHNAME);
        this.mapper.insert(other);

        List<DfvMvName> names = this.mapper.getAll();
        assertNotNull(names);
        assertEquals(3, names.size());

        this.mapper.deleteAll();
        assertNull(this.mapper.get(32234567));
        assertNull(this.mapper.get(41234568));
        assertNull(this.mapper.get(51234569));
    }

    @Test(expected = PersistenceException.class)
    public void testPrimaryKeyConstraintViolation() throws Exception {
        try {
            this.mapper.insert(this.dfvMvName);
            this.mapper.insert(this.dfvMvName);
        } finally {
            // avoids an exception in case this test is run before the test()
            // method.
            this.mapper.deleteAll();
        }
    }

}
