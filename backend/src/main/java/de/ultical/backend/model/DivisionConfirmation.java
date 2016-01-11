package de.ultical.backend.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;

@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = DivisionConfirmation.class)
public abstract class DivisionConfirmation {
    // define division about gender and age
    private DivisionType divisionType;
    private DivisionAge divisionAge;
}
