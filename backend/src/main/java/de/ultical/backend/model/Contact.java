package de.ultical.backend.model;

import de.ultical.backend.data.mapper.ContactMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Contact extends Identifiable {
    private String email;
    private String name;
    private String phone;

    @Override
    public Class<ContactMapper> getMapper() {
        return ContactMapper.class;
    }
}
