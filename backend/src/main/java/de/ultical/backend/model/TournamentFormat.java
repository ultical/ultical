package de.ultical.backend.model;

import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class TournamentFormat {

	private int id;

	private String name;

	private String description;

	private List<TournamentEdition> editions;

	private Set<User> admins;

}
