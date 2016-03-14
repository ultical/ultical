package de.ultical.backend.model;

import java.util.List;

import de.ultical.backend.data.mapper.RosterMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Roster extends Identifiable {

    private Team team;

    private Season season;
    private DivisionAge divisionAge;
    private DivisionType divisionType;

    private String nameAddition;

    private Context context;

    private List<RosterPlayer> players;

    @Override
    public Class<RosterMapper> getMapper() {
        return RosterMapper.class;
    }
}
