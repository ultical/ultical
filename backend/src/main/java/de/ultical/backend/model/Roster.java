package de.ultical.backend.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import de.ultical.backend.data.mapper.RosterMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Roster extends Identifiable {

    @JsonBackReference
    private Team team;

    private Season season;
    private DivisionAge divisionAge;
    private DivisionType divisionType;
    private List<RosterPlayer> players;

    @Override
    public Class<RosterMapper> getMapper() {
        return RosterMapper.class;
    }
}
