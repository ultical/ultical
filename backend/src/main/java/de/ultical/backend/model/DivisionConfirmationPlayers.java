package de.ultical.backend.model;

import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

// player confirmation - for multi matchday HAT tournaments, etc.

@Data
@EqualsAndHashCode(callSuper = true)
public class DivisionConfirmationPlayers extends DivisionConfirmation {
	private Set<Player> players;

}
