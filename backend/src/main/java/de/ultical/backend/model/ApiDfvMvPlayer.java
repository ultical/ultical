package de.ultical.backend.model;

import lombok.Data;

@Data
public class ApiDfvMvPlayer {
	private String geburtsdatum;
	private String verein;
	private boolean ruht;
	private boolean aktiv;
	private int dfvnr;
	private String geschlecht;
	private boolean dse;
	private String email;
}
