package de.ultical.backend.model;

import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

// team confirmation - for multi matchday team tournaments

@Data
@EqualsAndHashCode(callSuper = true)
public class DivisionConfirmationTeams extends DivisionConfirmation {
	private Set<Team> teams;
}
