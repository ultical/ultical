package de.ultical.backend.model;

import lombok.Data;

@Data
public class Season {
	private Surface surface;
	private int year;
	private boolean plusOneYear = false;
}
