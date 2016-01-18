package de.ultical.backend.model;

import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TournamentEditionLeague extends TournamentEdition {

    private Set<Event> events;
    private String alternativeMatchdayName;
}
