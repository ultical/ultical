package de.ultical.backend.model;

import java.util.Set;

import lombok.Data;

@Data
public class Roster {
	private Team team;
	private Season season;
	private Set<Player> players;
}
