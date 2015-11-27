package de.ultical.backend.model;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

// team registration - for team tournaments

@Data
@EqualsAndHashCode(callSuper = true)
public class DivisionRegistrationTeams extends DivisionRegistration {
	// ordered list of registered teams
	private Map<Integer, TeamRegistration> registeredTeams;
}
