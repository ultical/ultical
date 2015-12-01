package de.ultical.backend.model;

import java.util.Set;

import org.joda.time.LocalDate;

import io.dropwizard.validation.MinSize;
import lombok.Data;

@Data
public class Event {

	private int id;

	// keep on -1 for single tournaments
	private int matchdayNumber = -1;

	private TournamentEdition tournamentEdition;

	private Location location;

	// subset of the tournaments divisions and participants
	@MinSize(1)
	private Set<DivisionConfirmation> divisionConfirmations;

	private LocalDate startDate;
	private LocalDate endDate;

	private Set<User> admins;

}
