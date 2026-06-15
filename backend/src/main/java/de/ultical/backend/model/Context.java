package de.ultical.backend.model;

import de.ultical.backend.data.mapper.ContextMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Context extends Identifiable {
    private String name;
    private String acronym;

    /**
     * The association this context belongs to. A {@code null} association marks
     * a universal context that is valid for every tournament format.
     */
    private Association association;

    @Override
    public Class<ContextMapper> getMapper() {
        return ContextMapper.class;
    }
}