package de.ultical.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public abstract class DivisionRegistration extends Identifiable {
	// define division about gender and age
	private DivisionType divisionType;
	private DivisionAge divisionAge;

	private int numberSpots;
}
