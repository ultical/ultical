package de.ultical.backend.model;

import java.util.List;

import lombok.Data;

@Data
public class Division {
	private DivisionType divisionType;
	private List<Team> teams;
	private int id;
}
