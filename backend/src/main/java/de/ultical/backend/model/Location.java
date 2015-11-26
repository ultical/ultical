package de.ultical.backend.model;

import lombok.Data;

@Data
public class Location {

	private double longitude, latitude;
	private String city;
	private String country;
	private String street;
	private int zipcode;
	private String additionalInfo;

}
