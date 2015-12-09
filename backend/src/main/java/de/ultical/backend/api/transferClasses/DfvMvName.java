package de.ultical.backend.api.transferClasses;

import lombok.Data;

@Data
public class DfvMvName {
	private String nachname;
	private String vorname;
	private boolean dse;
	private int dfvnr;
}
