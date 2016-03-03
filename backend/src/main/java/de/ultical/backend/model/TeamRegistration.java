package de.ultical.backend.model;

import java.time.LocalDateTime;

import de.ultical.backend.data.mapper.TeamRegistrationMapper;
import de.ultical.backend.model.DivisionRegistration.DivisionRegistrationStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeamRegistration extends Identifiable {
    private Team team;
    private LocalDateTime timeRegistered;
    private String comment;
    private int standing;
    private float spiritScore;
    private boolean paid;
    private DivisionRegistrationStatus status;
    private boolean notQualified;
    private int sequence;
    private Roster roster;

    @Override
    public Class<TeamRegistrationMapper> getMapper() {
        return TeamRegistrationMapper.class;
    }
}
