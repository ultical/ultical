package de.ultical.backend.model;

import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.ultical.backend.app.LocalDateDeserializer;
import de.ultical.backend.app.LocalDateSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public abstract class TournamentEdition extends Identifiable{

	@JsonBackReference
	private TournamentFormat tournamentFormat;

	private String alternativeName;
	private Season season;
	
	@JsonSerialize(using=LocalDateSerializer.class)
	@JsonDeserialize(using=LocalDateDeserializer.class)
	private LocalDate registrationStart;
	@JsonSerialize(using=LocalDateSerializer.class)
	@JsonDeserialize(using=LocalDateDeserializer.class)
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

	private Set<DivisionRegistration> divisionRegistrations;
}
