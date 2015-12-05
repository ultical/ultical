package de.ultical.backend.model;

import lombok.Data;

@Data
public class ApiDfvMvName {
	private String nachname;
	private String vorname;
	private boolean dse;
	private int dfvnr;
}
