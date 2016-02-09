package de.ultical.backend.model;

import java.util.List;

import de.ultical.backend.data.mapper.DivisionConfirmationMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

// player confirmation - for multi matchday HAT tournaments, etc.

@Data
@EqualsAndHashCode(callSuper = true)
public class DivisionConfirmationPlayers extends DivisionConfirmation {
    private List<Player> players;

    @Override
    public Class<DivisionConfirmationMapper> getMapper() {
        return DivisionConfirmationMapper.class;
    }
}
