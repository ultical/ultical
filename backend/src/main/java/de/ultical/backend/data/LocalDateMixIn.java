package de.ultical.backend.data;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.chrono.Era;
import java.time.chrono.IsoChronology;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * Provides serialization for java.time.LocalDate in a way yyyy-MM-dd
 */
public interface LocalDateMixIn {

	@Override
	@JsonProperty("string")
	String toString();

	@JsonIgnore
	IsoChronology getChronology();

	@JsonIgnore
	int getYear();

	@JsonIgnore
	Month getMonth();

	@JsonIgnore
	int getDayOfMonth();

	@JsonIgnore
	DayOfWeek getDayOfWeek();

	@JsonIgnore
	int getDayOfYear();

	@JsonIgnore
	int getMonthValue();

	@JsonIgnore
	Era getEra();

	@JsonIgnore
	boolean isLeapYear();
}
