package de.ultical.backend.model;

import java.util.List;

import de.ultical.backend.data.mapper.AssociationMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Association extends Identifiable {
    private String name;
    private int id;
    private int version;
    private String acronym;

    private Contact contact;

    private List<User> admins;

    @Override
    public Class<AssociationMapper> getMapper() {
        return AssociationMapper.class;
    }
}