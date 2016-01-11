package de.ultical.backend.model;

import java.util.Map;

import de.ultical.backend.data.mapper.DivisionRegistrationMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

// player registration - for HAT tournaments, etc.

@Data
@EqualsAndHashCode(callSuper = true)
public class DivisionRegistrationPlayers extends DivisionRegistration {
    // ordered list of registered players
    private Map<Integer, PlayerRegistration> registeredPlayers;

    @Override
    public Class<DivisionRegistrationMapper> getMapper() {
        return DivisionRegistrationMapper.class;
    }
}
