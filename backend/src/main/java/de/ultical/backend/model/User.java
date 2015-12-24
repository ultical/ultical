package de.ultical.backend.model;

import java.security.Principal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.ultical.backend.data.mapper.UserMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class User extends Identifiable implements Principal {

    private String email;
    private String password;
    private DfvPlayer dfvPlayer;
    private boolean emailConfirmed;
    private boolean dfvEmailOptIn;

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
