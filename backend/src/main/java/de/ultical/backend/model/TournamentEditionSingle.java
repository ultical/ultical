package de.ultical.backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = "event")
public class TournamentEditionSingle extends TournamentEdition {

    @JsonManagedReference
    private Event event;
}
