package de.ultical.backend.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import de.ultical.backend.data.mapper.BaseMapper;
import lombok.Data;

@Data
@JsonIdentityInfo(generator = JSOGGenerator.class)
public abstract class Identifiable {

    private int id;
    private int version;

    @JsonIgnore
    public abstract Class<? extends BaseMapper<? extends Identifiable>> getMapper();
}
