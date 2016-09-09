package de.ultical.backend.model;

import de.ultical.backend.data.mapper.ContextMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Context extends Identifiable {
    private String name;
    private String acronym;

    @Override
    public Class<ContextMapper> getMapper() {
        return ContextMapper.class;
    }
}