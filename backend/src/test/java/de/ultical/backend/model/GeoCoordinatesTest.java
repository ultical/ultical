package de.ultical.backend.model;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class GeoCoordinatesTest {

	@Test
	public void testDistanceTo() {
		GeoCoordinates potsdam = new GeoCoordinates(52.4009309, 13.0591397);
		GeoCoordinates karlshagen = new GeoCoordinates(54.11241705, 13.8352242884203);
		final double distancePdmKhgn = potsdam.distanceTo(karlshagen);
		final double distanceKhgnPdm = karlshagen.distanceTo(potsdam);
		assertEquals(197200, distancePdmKhgn, 25);
		assertEquals(197200, distanceKhgnPdm, 25);
	}

	@Test
	public void testDistanceToNull() {
		GeoCoordinates coord = new GeoCoordinates(0, 0);
		assertThat(coord.distanceTo(null), equalTo(0.0));
	}
}
