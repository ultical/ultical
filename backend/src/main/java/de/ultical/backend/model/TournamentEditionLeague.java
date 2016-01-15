package de.ultical.backend.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TournamentEditionLeague extends TournamentEdition {

    @JsonManagedReference
    private Set<Event> events;
    private String alternativeMatchdayName;
}
