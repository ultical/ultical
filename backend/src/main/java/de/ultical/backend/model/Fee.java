package de.ultical.backend.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.ultical.backend.data.mapper.FeeMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Roster.class)
public class Fee extends Identifiable {
    private FeeType type;
    private String otherName;
    private double amount;
    private String currency;
    private Event event;
    private TournamentEdition tournamentEdition;
    private boolean perPerson;
    private boolean multiple;

    @Override
    public Class<FeeMapper> getMapper() {
        return FeeMapper.class;
    }
}
