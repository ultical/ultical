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

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import de.ultical.backend.model.Location;
import de.ultical.backend.utils.test.PrepareDBRule;

public class LocationMapperTest {

    private static final double LONG = 45.342343;

    private static final double LAT = 51.343434;

    private static final String ADDITIONAL_INFO = "le Karli";

    private static final String ZIPCODE = "14482";

    private static final String COUNTRY = "Germany";

    private static final String COUNTRY_CODE = "de";

    private static final String STREET = "Karl Liebknecht Str.";

    private static final String CITY = "Potsdam";

    @ClassRule
    public static PrepareDBRule DBRULE = new PrepareDBRule();

    private Location location;
    private LocationMapper mapper;

    @Before
    public void setUp() throws Exception {

        this.location = new Location();
        this.location.setCity(CITY);
        this.location.setStreet(STREET);
        this.location.setZipCode(ZIPCODE);
        this.location.setCountry(COUNTRY);
        this.location.setCountryCode(COUNTRY_CODE);
        this.location.setAdditionalInfo(ADDITIONAL_INFO);
        this.location.setLatitude(LAT);
        this.location.setLongitude(LONG);

        this.mapper = DBRULE.getSession().getMapper(this.location.getMapper());
    }

    @After
    public void tearDown() throws Exception {
        DBRULE.closeSession();
    }

    @Test
    public void test() {
        assertEquals(0, this.location.getId());
        this.mapper.insert(this.location);
        DBRULE.getSession().commit();
        // assertEquals(1, location.getId());

        final int locationId = this.location.getId();
        Location readLocation = this.mapper.get(locationId);
        assertNotNull(readLocation);
        assertEquals(CITY, readLocation.getCity());
        assertEquals(COUNTRY, readLocation.getCountry());
        assertEquals(COUNTRY_CODE, readLocation.getCountryCode());
        assertEquals(STREET, readLocation.getStreet());
        assertEquals(ZIPCODE, readLocation.getZipCode());
        assertEquals(ADDITIONAL_INFO, readLocation.getAdditionalInfo());
        assertEquals(LAT, readLocation.getLatitude(), 0);
        assertEquals(LONG, readLocation.getLongitude(), 0);
        assertEquals(1, readLocation.getVersion());

        int updCount = this.mapper.update(this.location);
        assertEquals(0, updCount);

        readLocation.setCity("Berlin");
        updCount = this.mapper.update(readLocation);
        assertEquals(1, updCount);

        readLocation = this.mapper.get(this.location.getId());
        assertNotNull(readLocation);
        assertEquals(2, readLocation.getVersion());
        assertEquals("Berlin", readLocation.getCity());
        assertEquals(ZIPCODE, readLocation.getZipCode());

        this.mapper.insert(readLocation);
        final int secondLocationId = readLocation.getId();
        DBRULE.getSession().commit();

        List<Location> allLocations = this.mapper.getAll();
        assertNotNull(allLocations);
        assertEquals(2, allLocations.size());

        this.mapper.delete(readLocation);
        assertNull(this.mapper.get(secondLocationId));
        DBRULE.getSession().commit();

        allLocations = this.mapper.getAll();
        assertNotNull(allLocations);
        assertEquals(1, allLocations.size());
    }

}
