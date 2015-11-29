package de.ultical.backend.model;

import lombok.Data;

@Data
public abstract class Player extends Identifiable {
	private String firstName;
	private String lastName;
	private Gender gender;
}
