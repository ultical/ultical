package de.ultical.backend.model;

import java.util.Date;

import lombok.Data;

@Data
public class AbstractTournament {

	private String name;
	private Date firstDay;
	private Date lastDay;

}
