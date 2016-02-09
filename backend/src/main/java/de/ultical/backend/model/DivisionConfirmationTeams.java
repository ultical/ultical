package de.ultical.backend.model;

import java.util.List;

import de.ultical.backend.data.mapper.DivisionConfirmationMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

// team confirmation - for multi matchday team tournaments

@Data
@EqualsAndHashCode(callSuper = true)
public class DivisionConfirmationTeams extends DivisionConfirmation {
    private List<TeamRegistration> teams;

    @Override
    public Class<DivisionConfirmationMapper> getMapper() {
        return DivisionConfirmationMapper.class;
    }
}
