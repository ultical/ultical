package de.ultical.backend.model;

import lombok.Data;

@Data
public abstract class DivisionRegistration {
	// define division about gender and age
	private DivisionType divisionType;
	private DivisionAge divisionAge;

	private int numberSpots;
}
