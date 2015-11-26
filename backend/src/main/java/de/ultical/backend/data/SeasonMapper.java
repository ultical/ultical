package de.ultical.backend.data;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import de.ultical.backend.model.Season;

public interface SeasonMapper {

	@Select("SELECT * FROM season")
	public Season getSeasons();

	@Insert("Insert into season (surface, year, end_year) VALUES (#{surface}, #{year}, #{endYear}")
	public void addSeason(Season season);
}
