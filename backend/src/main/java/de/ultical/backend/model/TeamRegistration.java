package de.ultical.backend.model;

import org.joda.time.DateTime;

import lombok.Data;

@Data
public class TeamRegistration {
	private Team team;
	private DateTime timeRegistered;
	private String comment;
}
