package de.ultical.backend.exception;

public class TravelCompensationException extends UlticalException {

	private static final long serialVersionUID = -5648638307905720087L;

	public static TravelCompensationException noLocationsFound(final int eventId) {
		return new TravelCompensationException(String.format("For event %d no locations have been found", eventId));
	}

	public static TravelCompensationException noMainLocationForEvent(final int eventId) {
		return new TravelCompensationException(
				String.format("For event %d no main-location has been defined", eventId));
	}

	public TravelCompensationException(String message) {
		super(message);
	}

}
