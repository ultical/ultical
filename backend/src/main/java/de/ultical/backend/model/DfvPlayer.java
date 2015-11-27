package de.ultical.backend.model;

import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DfvPlayer extends Player {
	private String dfvNumber;
	private int id;
	private String biography;
	private LocalDate birthDate;
	private Gender gender;
}
