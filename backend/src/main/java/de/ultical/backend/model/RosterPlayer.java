package de.ultical.backend.model;

import java.time.LocalDate;

import lombok.Data;

@Data
public class RosterPlayer {
    private Roster roster;
    private Player player;
    private LocalDate dateAdded;
}
