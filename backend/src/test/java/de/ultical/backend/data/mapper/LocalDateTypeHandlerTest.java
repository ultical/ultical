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
