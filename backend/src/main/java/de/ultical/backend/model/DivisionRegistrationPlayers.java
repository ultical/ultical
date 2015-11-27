package de.ultical.backend.model;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

// player registration - for HAT tournaments, etc.

@Data
@EqualsAndHashCode(callSuper = true)
public class DivisionRegistrationPlayers extends DivisionRegistration {
	// ordered list of registered players
	private Map<Integer, PlayerRegistration> registeredPlayers;

}
