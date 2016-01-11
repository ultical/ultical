package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import de.ultical.backend.model.Roster;

public interface RosterMapper extends BaseMapper<Roster> {

    final String SELECT_STMT = "SELECT id, version, team, season, division_age as divisionAge, division_type as divisionType FROM ROSTER";

    // INSERT
    @Override
    @Insert("INSERT INTO ROSTER (team, season, division_age, division_type) VALUES (#{team.id},#{season.id},#{divisionAge},#{divisionType})")
    Integer insert(Roster entity);

    // UPDATE
    @Override
    @Update("UPDATE ROSTER SET team = #{team.id}, season = #{season.id}, division_age = #{divisionAge}, division_type = #{divisionType} WHERE id = #{id} AND version = #{version}")
    Integer update(Roster entity);

    // DELETE
    @Override
    @Delete("DELETE FROM ROSTER WHERE id=#{rosterId}")
    void delete(int rosterId);

    // SELECT
    @Override
    @Select({ SELECT_STMT, "WHERE id = #{id}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "division_age", property = "division_age"),
            @Result(column = "division_type", property = "division_type"),
            @Result(column = "team", property = "team", one = @One(select = "de.ultical.backend.data.mapper.TeamMapper.get") ),
            @Result(column = "season", property = "season", one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
            @Result(column = "id", property = "players", many = @Many(select = "de.ultical.backend.data.mapper.PlayerMapper.getByRoster") ) })
    Roster get(int id);

    @Override
    @Select(SELECT_STMT)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "division_age", property = "division_age"),
            @Result(column = "division_type", property = "division_type"),
            @Result(column = "season", property = "season", one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
            @Result(column = "id", property = "players", many = @Many(select = "de.ultical.backend.data.mapper.PlayerMapper.getByRoster") ) })
    List<Roster> getAll();

    @Select({ SELECT_STMT, "WHERE team = #{teamId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "division_age", property = "division_age"),
            @Result(column = "division_type", property = "division_type"),
            @Result(column = "season", property = "season", one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
            @Result(column = "id", property = "players", many = @Many(select = "de.ultical.backend.data.mapper.PlayerMapper.getByRoster") ) })
    List<Roster> getForTeam(Integer teamId);

}
