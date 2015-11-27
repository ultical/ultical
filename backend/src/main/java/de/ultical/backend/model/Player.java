package de.ultical.backend.model;

import lombok.Data;

@Data
public abstract class Player {
	private String firstName;
	private String lastName;
	private Gender gender;
}
