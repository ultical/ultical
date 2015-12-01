package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;

import de.ultical.backend.model.Season;

public interface SeasonMapper extends BaseMapper<Season> {

	@Select({ "SELECT * FROM SEASON WHERE id=#{id}" })
	@Results({ @Result(property = "id", column = "id"), @Result(column = "version", property = "version"),
			@Result(column = "surface", property = "surface"), @Result(column = "season_year", property = "year"),
			@Result(column = "plusOneYear", property = "plusOneYear") })
	Season get(int id);

	@Select("SELECT * from SEASON")
	@Results({ @Result(property = "id", column = "id"), @Result(column = "version", property = "version"),
			@Result(column = "surface", property = "surface"), @Result(column = "season_year", property = "year"),
			@Result(column = "plusOneYear", property = "plusOneYear") })
	List<Season> getAll();

	@Insert({"INSERT INTO SEASON (surface, season_year, plusOneYear)",
		"VALUES (#{surface},#{year},#{plusOneYear})"})
	@Options(useGeneratedKeys=true, keyProperty="id")
	Integer insert(Season season);

	@Update({"UPDATE SEASON SET",
		"version=version+1, surface=#{surface}, season_year=#{year}, plusOneYear=#{plusOneYear}",
		"WHERE id=#{id} AND version=#{version}"})
	Integer update(Season season);

	@Delete("DELETE FROM SEASON WHERE id=#{id}")
	void delete(Season season);
}
