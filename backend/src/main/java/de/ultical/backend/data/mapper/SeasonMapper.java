package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import de.ultical.backend.model.Season;

public interface SeasonMapper extends BaseMapper<Season> {

	@Override
    @Select({ "SELECT * FROM SEASON WHERE id=#{id}" })
	@Results({ @Result(property = "id", column = "id"), @Result(column = "version", property = "version"),
			@Result(column = "surface", property = "surface"), @Result(column = "season_year", property = "year"),
			@Result(column = "plusOneYear", property = "plusOneYear") })
	Season get(int id);

	@Override
    @Select("SELECT * FROM SEASON")
	@Results({ @Result(property = "id", column = "id"), @Result(column = "version", property = "version"),
			@Result(column = "surface", property = "surface"), @Result(column = "season_year", property = "year"),
			@Result(column = "plusOneYear", property = "plusOneYear") })
	List<Season> getAll();

	@Select("SELECT * FROM SEASON WHERE surface=#{surface} AND season_year=#{year} AND plusOneYear=#{plusOneYear}")
	@Results({ @Result(property = "id", column = "id"), @Result(column = "version", property = "version"),
			@Result(column = "surface", property = "surface"), @Result(column = "season_year", property = "year"),
			@Result(column = "plusOneYear", property = "plusOneYear") })
	Season getByProperties(Season season);

	@Override
    @Insert({"INSERT INTO SEASON (surface, season_year, plusOneYear)",
		"VALUES (#{surface},#{year},#{plusOneYear})"})
	@Options(useGeneratedKeys=true, keyProperty="id")
	Integer insert(Season season);

	@Override
    @Update({"UPDATE SEASON SET",
		"version=version+1, surface=#{surface}, season_year=#{year}, plusOneYear=#{plusOneYear}",
		"WHERE id=#{id} AND version=#{version}"})
	Integer update(Season season);

	@Override
    @Delete("DELETE FROM SEASON WHERE id=#{id}")
	void delete(Season season);
}
