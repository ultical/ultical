package de.ultical.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = "event")
public class TournamentEditionSingle extends TournamentEdition {

    private Event event;
}
