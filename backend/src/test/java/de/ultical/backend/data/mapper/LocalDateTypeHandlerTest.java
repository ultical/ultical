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
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LocalDateTypeHandlerTest {

	private static final String DATE_COLUMN = "dateColumn";
	private Date sqlDate;
	private LocalDateTypeHandler localDateTypeHandler;
	@Mock
	private ResultSet resultSet;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.sqlDate = Date.valueOf("2015-11-29");
		this.localDateTypeHandler = new LocalDateTypeHandler();
		when(resultSet.getDate(DATE_COLUMN)).thenReturn(this.sqlDate);
	}

	@Test
	public void testGetResult() throws Exception{
		LocalDate result = this.localDateTypeHandler.getResult(resultSet, DATE_COLUMN);
		assertNotNull(result);
		assertEquals(2015,result.getYear());
		assertEquals(11,result.getMonthValue());
		assertEquals(29,result.getDayOfMonth());
	}

}
