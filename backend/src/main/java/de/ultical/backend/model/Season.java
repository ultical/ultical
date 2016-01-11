package de.ultical.backend.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.ultical.backend.data.mapper.SeasonMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Season.class)
public class Season extends Identifiable {
    private Surface surface;
    private int year;
    private boolean plusOneYear = false;

    @Override
    public Class<SeasonMapper> getMapper() {
        return SeasonMapper.class;
    }
}
