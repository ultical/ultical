package de.ultical.backend.model;

import java.security.Principal;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.ultical.backend.data.mapper.UserMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = User.class)
public class User extends Identifiable implements Principal {

    private String email;
    private String password;
    private DfvPlayer dfvPlayer;
    private boolean emailConfirmed;
    private boolean dfvEmailOptIn;

    public String getFullName() {
        if (this.dfvPlayer != null) {
            return this.getDfvPlayer().getFirstName() + " " + this.getDfvPlayer().getLastName();
        } else {
            return this.getEmail();
        }
    }

    @Override
    public Class<UserMapper> getMapper() {
        return UserMapper.class;
    }

    @Override
    @JsonIgnore
    public String getName() {
        return this.email;
    }
}
