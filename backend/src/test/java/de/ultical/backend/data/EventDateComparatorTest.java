package de.ultical.backend.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.ultical.backend.model.Event;

public class EventDateComparatorTest {

	private EventDateComparator edc;
	@Mock
	private Event eventStartsNow;
	@Mock
	private Event eventStartDateNull;
	@Mock
	private Event eventStartsLater;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.edc = new EventDateComparator();
		final Instant now = Instant.now();
		when(eventStartsNow.getStartDate()).thenReturn(Date.from(now));
		final Instant fiveHoursLater = now.plusSeconds(5*60*60);
		when(eventStartsLater.getStartDate()).thenReturn(Date.from(fiveHoursLater));
	}

	@Test
	public void testBothNull() {
		assertEquals(-1, this.edc.compare(null, null));
	}

	@Test
	public void testFirstArgStartDateNull() {
		assertEquals(-1, this.edc.compare(eventStartDateNull, null));
	}

	@Test
	public void testSecondArgNull() {
		assertEquals(1, this.edc.compare(eventStartsNow, null));
	}

	@Test
	public void testSecondArgStartDateNull() {
		assertEquals(1, this.edc.compare(eventStartsNow, eventStartDateNull));
	}
	
	@Test
	public void testFirstIsBefore() {
		assertTrue(this.edc.compare(eventStartsNow, eventStartsLater)<=-1);
	}
	
	@Test
	public void testSecondIsFirst() {
		assertTrue(this.edc.compare(eventStartsLater, eventStartsNow)>=1);
	}
	
	@Test
	public void testComparatorContract() {
		assertEquals(this.edc.compare(eventStartsNow, eventStartsLater), -1 * this.edc.compare(eventStartsLater, eventStartsNow));
	}
}
