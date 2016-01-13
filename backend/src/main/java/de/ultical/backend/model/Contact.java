package de.ultical.backend.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.ultical.backend.data.mapper.ContactMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Contact.class)
public class Contact extends Identifiable {
    private String email;
    private String name;
    private String phone;

    @Override
    public Class<ContactMapper> getMapper() {
        return ContactMapper.class;
    }
}
