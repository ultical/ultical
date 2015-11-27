package de.ultical.backend.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TeamRegistration {
	private Team team;
	private LocalDateTime timeRegistered;
	private String comment;
}
