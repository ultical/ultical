package de.ultical.backend.model;

import java.util.Date;
import java.util.List;
import java.util.Set;

import io.dropwizard.validation.MinSize;
import lombok.Data;

@Data
public class Event {
	@MinSize(1)
	private List<Division> divisions;
	private AbstractTournament tournament;
	
	private Set<TeamPlayerAssignment> players;
	
	private Date startDate;
	private Date endDate;
}
