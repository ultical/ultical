package de.ultical.backend.model;

import java.time.LocalDate;
import java.util.Set;

import de.ultical.backend.data.mapper.EventMapper;
import io.dropwizard.validation.MinSize;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class Event extends Identifiable{

	// keep on -1 for single tournaments
	private int matchdayNumber = -1;

	private TournamentEdition tournamentEdition;

	private Location location;

	// subset of the tournaments divisions and participants
	@MinSize(1)
	private Set<DivisionConfirmation> divisionConfirmations;

	private LocalDate startDate;
	private LocalDate endDate;

	private int feePerTeam;
	private int feePerPlayer;
	private int feePerGuest;
	private int feePerBreakfast;
	private int feePerLunch;
	private int feePerDinner;
	private int feePerNight;

	private Set<User> admins;

	private String localOrganizerName;
	private String localOrganizerEmail;
	private String localOrganizerPhone;
	
	@Override
	public Class<EventMapper> getMapper() {
		return EventMapper.class;
	}
}
