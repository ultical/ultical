package de.ultical.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UnregisteredPlayer extends Player {
	private int id;
	private String email;
}
