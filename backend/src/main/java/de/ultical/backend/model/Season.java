package de.ultical.backend.model;

import de.ultical.backend.data.mapper.SeasonMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Season extends Identifiable {
    private Surface surface;
    private int year;
    private boolean plusOneYear = false;

    @Override
    public Class<SeasonMapper> getMapper() {
        return SeasonMapper.class;
    }
}
