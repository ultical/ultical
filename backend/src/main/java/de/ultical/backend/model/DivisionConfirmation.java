package de.ultical.backend.model;

import lombok.Data;

@Data
public abstract class DivisionConfirmation {
    // define division about gender and age
    private DivisionType divisionType;
    private DivisionAge divisionAge;
}
