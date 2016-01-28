package de.ultical.backend.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = DivisionRegistration.class)
public abstract class DivisionRegistration extends Identifiable {

    public enum DivisionRegistrationStatus {
        PENDING, CONFIRMED, WAITING_LIST, DECLINED
    }

    // define division about gender and age
    private DivisionType divisionType;
    private DivisionAge divisionAge;
    private String divisionIdentifier;

    private int numberSpots;
}
