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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import de.ultical.backend.model.Contact;
import de.ultical.backend.model.Season;
import de.ultical.backend.model.Surface;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.TournamentFormat;
import de.ultical.backend.utils.test.PrepareDBRule;

public class TournamentEditionMapperTest {

    private static final LocalDate END_DATE = LocalDate.of(2015, 11, 1);
    private static final LocalDate START_DATE = LocalDate.of(2015, 10, 1);
    private static final String NAME = "mein Test";
    private static final String UPDATED_NAME = "different name";
    TournamentEdition edition;
    TournamentEditionMapper mapper;

    @ClassRule
    public static PrepareDBRule DBRULE = new PrepareDBRule();

    @Before
    public void setUp() throws Exception {
        this.edition = new TournamentEdition();

        TournamentFormat format = new TournamentFormat();
        format.setName("test format");
        format.setDescription("");
        DBRULE.getSession().getMapper(format.getMapper()).insert(format);
        Season season = new Season();
        season.setSurface(Surface.TURF);
        season.setYear(2015);
        DBRULE.getSession().getMapper(season.getMapper()).insert(season);

        Contact organizer = new Contact();
        organizer.setName("e.V.");
        DBRULE.getSession().getMapper(organizer.getMapper()).insert(organizer);
        this.edition.setOrganizer(organizer);

        this.edition.setTournamentFormat(format);
        this.edition.setName(NAME);
        this.edition.setSeason(season);
        this.edition.setRegistrationStart(START_DATE);
        this.edition.setRegistrationEnd(END_DATE);

        this.mapper = DBRULE.getSession().getMapper(this.edition.getMapper());
        ContactMapper cm = DBRULE.getSession().getMapper(ContactMapper.class);
        cm.insert(organizer);

    }

    @Test
    public void test() {
        assertEquals(0, this.edition.getId());
        this.mapper.insert(this.edition);
        DBRULE.getSession().commit();
        final int editionId = this.edition.getId();

        TournamentEdition readEdition = this.mapper.get(editionId);
        assertNotNull(readEdition);
        assertTrue(readEdition instanceof TournamentEdition);
        assertNotNull(readEdition.getTournamentFormat());
        assertNotNull(readEdition.getSeason());
        assertNotNull(readEdition.getOrganizer());
        assertEquals(NAME, readEdition.getName());
        assertEquals(1, readEdition.getVersion());

        int updateCount = this.mapper.update(this.edition);
        assertEquals(0, updateCount);

        readEdition.setName(UPDATED_NAME);
        updateCount = this.mapper.update(readEdition);
        assertEquals(1, updateCount);
        readEdition = this.mapper.get(editionId);
        assertNotNull(readEdition);
        assertTrue(readEdition instanceof TournamentEdition);
        assertNotNull(readEdition.getTournamentFormat());
        assertNotNull(readEdition.getSeason());
        assertEquals(UPDATED_NAME, readEdition.getName());
        assertNotNull(readEdition.getOrganizer());

        this.mapper.insert(this.edition);
        assertNotEquals(this.edition.getId(), readEdition.getId());
        List<TournamentEdition> allEditions = this.mapper.getAll();
        assertNotNull(allEditions);
        assertEquals(2, allEditions.size());

        this.mapper.delete(readEdition);
        allEditions = this.mapper.getAll();
        assertNotNull(allEditions);
        assertEquals(1, allEditions.size());

    }

}
