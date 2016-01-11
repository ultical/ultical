package de.ultical.backend.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PlayerRegistration {
    private Player player;
    private LocalDateTime timeRegistered;
    private String comment;
}
