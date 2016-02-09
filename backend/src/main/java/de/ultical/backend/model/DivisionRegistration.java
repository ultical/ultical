package de.ultical.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
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
