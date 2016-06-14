package de.ultical.backend.model;

import de.ultical.backend.data.mapper.ResourceMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Resource extends Identifiable {

    private String title;
    private String path;
    private boolean local;
    private Event event;

    @Override
    public Class<ResourceMapper> getMapper() {
        return ResourceMapper.class;
    }
}
