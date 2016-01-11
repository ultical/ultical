package de.ultical.backend.model;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.ultical.backend.data.mapper.TournamentFormatMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = TournamentFormat.class)
public class TournamentFormat extends Identifiable {

    private String name;

    private String description;

    @JsonManagedReference
    private List<TournamentEdition> editions;

    private Set<User> admins;

    @Override
    public Class<TournamentFormatMapper> getMapper() {
        return TournamentFormatMapper.class;
    }
}
