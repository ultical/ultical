package de.ultical.backend.model;

public class GeoCoordinates {
	private final static long R = 6371000;

	public final double latitude;
	public final double longitude;

	public GeoCoordinates(final double lat, final double longi) {
		this.latitude = lat;
		this.longitude = longi;
	}

	public double distanceTo(final GeoCoordinates tgt) {
		double result = 0;
		if (tgt != null) {
			final double srcRad = Math.toRadians(this.latitude);
			final double tgtRad = Math.toRadians(tgt.latitude);
			final double diffLat = Math.toRadians(this.latitude - tgt.latitude);
			final double diffLong = Math.toRadians(this.longitude - tgt.longitude);
			final double a = Math.pow(Math.sin(diffLat / 2), 2.0)
					+ Math.cos(srcRad) * Math.cos(tgtRad) * Math.pow(Math.sin(diffLong / 2), 2);
			final double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

			result = R * c;
		}
		return result;
	}
}
