package de.ultical.backend.model;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TournamentEditionLeague extends TournamentEdition {
	private Map<Integer, Event> events;
	private String alternativeMatchdayName;
}
