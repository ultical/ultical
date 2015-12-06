package de.ultical.backend.model;

import java.time.LocalDate;
import java.util.Set;

import io.dropwizard.validation.MinSize;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public abstract class TournamentEdition extends Identifiable{

	private TournamentFormat tournamentFormat;

	private String alternativeName;
	private Season season;

	private LocalDate registrationStart;
	private LocalDate registrationEnd;

	// fees should only be put here for a general league fee
	// (in addition to the event fees on each matchday)
	private int feePerPlayer;
	private int feePerTeam;
	private int feePerGuest;
	private String currency = "EUR";

	private String organizerName;
	private String organizerEmail;
	private String organizerPhone;

	@MinSize(1)
	private Set<DivisionRegistration> divisionRegistrations;
}
