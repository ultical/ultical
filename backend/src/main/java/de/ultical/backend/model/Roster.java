package de.ultical.backend.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

import de.ultical.backend.data.mapper.TeamMapper;
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
    private Set<Player> players;

    @Override
    public Class<TeamMapper> getMapper() {
        return TeamMapper.class;
    }
}
