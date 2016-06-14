package de.ultical.backend.model;

import java.util.List;

import de.ultical.backend.data.mapper.DivisionRegistrationMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

// team registration - for team tournaments

@Data
@EqualsAndHashCode(callSuper = true)
public class DivisionRegistrationTeams extends DivisionRegistration {
	// ordered list of registered teams
	// BB I think it is sufficient to use a List here, which is inherently ordered.
	private List<TeamRegistration> registeredTeams;

	@Override
    public Class<DivisionRegistrationMapper> getMapper() {
		return DivisionRegistrationMapper.class;
	}
}
