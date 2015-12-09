package de.ultical.backend.api.transferClasses;

import lombok.Data;

@Data
public class DfvMvPlayer {
	private String geburtsdatum;
	private String verein;
	private boolean ruht;
	private boolean aktiv;
	private int dfvnr;
	private String geschlecht;
	private boolean dse;
	private String email;
}
