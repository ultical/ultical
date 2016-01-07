package de.ultical.backend.data.mapper;

import java.util.List;

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

    @Override
    @Select({ SELECT_STMT, "WHERE id = #{id}" })
    @Results({ @Result(column = "division_age", property = "division_age"),
            @Result(column = "division_type", property = "division_type"),
            @Result(column = "season", property = "season", one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
            @Result(column = "id", property = "players", many = @Many(select = "de.ultical.backend.data.mapper.PlayersMapper.getByRoster") ) })
    Roster get(int id);

    @Override
    @Select(SELECT_STMT)
    List<Roster> getAll();

    @Override
    @Insert("INSERT INTO ROSTER (team, season, division_age, division_type) VALUES (#{team.id},#{season.id},#{divisionAge.name},#{divisionType.name})")
    Integer insert(Roster entity);

    @Override
    @Update("UPDATE ROSTER SET team = #{team.id}, season = #{season.id}, division_age = #{divisionAge.name}, division_type = #{divisionType.name} WHERE id = #{id} AND version = #{version}")
    Integer update(Roster entity);

    @Select({ SELECT_STMT, "WHERE team = #{teamId}" })
    @Results({ @Result(column = "division_age", property = "division_age"),
            @Result(column = "division_type", property = "division_type"),
            @Result(column = "season", property = "season", one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
            @Result(column = "id", property = "players", many = @Many(select = "de.ultical.backend.data.mapper.PlayersMapper.getByRoster") ) })
    List<Roster> getForTeam(Integer teamId);

}
