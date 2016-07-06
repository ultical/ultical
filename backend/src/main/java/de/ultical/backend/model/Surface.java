package de.ultical.backend.model;

public enum Surface {
	GYM("indoor"), TURF("outdoor"), BEACH("beach");
	
	private final String userFriendlyName;
	
	private Surface(final String ufName) {
		this.userFriendlyName = ufName;
	}
	
	public String toString() {
		return this.userFriendlyName;
	}
}
