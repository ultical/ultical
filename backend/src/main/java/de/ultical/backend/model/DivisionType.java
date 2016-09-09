package de.ultical.backend.model;

public enum DivisionType {
	OPEN("open"), MIXED("mixed"), WOMEN("Damen");
	
	private final String userFriendlyName;
	
	private DivisionType(final String ufName) {
		this.userFriendlyName = ufName;
	}
	
	public String toString() {
		return this.userFriendlyName;
	}
}
