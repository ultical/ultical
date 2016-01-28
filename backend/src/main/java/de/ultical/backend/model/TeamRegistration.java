package de.ultical.backend.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.ultical.backend.data.mapper.TeamRegistrationMapper;
import de.ultical.backend.model.DivisionRegistration.DivisionRegistrationStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = TeamRegistration.class)
public class TeamRegistration extends Identifiable {
    private Team team;
    private LocalDateTime timeRegistered;
    private String comment;
    private int standing;
    private float spiritPoints;
    private boolean paid;
    private DivisionRegistrationStatus status;
    private boolean notQualified;

    @Override
    public Class<TeamRegistrationMapper> getMapper() {
        return TeamRegistrationMapper.class;
    }
}
