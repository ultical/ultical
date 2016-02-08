package de.ultical.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class Player extends Identifiable {
    private String firstName;
    private String lastName;
    private Gender gender;
    private String email;
}
