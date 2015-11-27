package de.ultical.backend.model;

import org.joda.time.DateTime;

import lombok.Data;

@Data
public class PlayerRegistration {
	private Player player;
	private DateTime timeRegistered;
	private String comment;
}
