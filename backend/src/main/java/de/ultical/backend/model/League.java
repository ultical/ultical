package de.ultical.backend.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class League extends AbstractTournament {

	private static final String DEFAULT_EVENT_NAMES = "Spieltag";
	private List<Event> leagueEvents;
	private String eventNames = DEFAULT_EVENT_NAMES;
}
