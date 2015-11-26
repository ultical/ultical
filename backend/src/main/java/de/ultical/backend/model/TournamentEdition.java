package de.ultical.backend.model;

import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;

import lombok.Data;

@Data
public class TournamentEdition {

	private TournamentFormat tournamentFormat;

	private String alternativeName;
	private Season season;

	// TODO: really right here?
	private LocalDate registrationStart;
	private LocalDate registrationStop;

	private Set<Division> divisions;

	private List<Event> events;

}
