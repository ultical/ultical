package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import de.ultical.backend.model.Team;

public interface TeamMapper extends BaseMapper<Team> {

    // INSERT
    @Override
    @Insert("INSERT INTO TEAM (name, description, founding_date, location) VALUES #{name}, #{description}, #{foundingDate}, #{location.id}")
    @Options(keyProperty = "id", useGeneratedKeys = true)
    Integer insert(Team team);

    // UPDATE
    @Override
    @Update("UPDATE TEAM SET version=version+1, name=#{name}, description=#{description}, founding_date=#{foundingDate}, location=#{location.id} WHERE version=#{version} AND id=#{id}")
    Integer update(Team t);

    // DELETE
    @Override
    @Delete("DELETE FORM TEAM WHERE id=#{team.id}")
    void delete(Team team);

    // SELECT
    @Override
    @Select("SELECT * FROM TEAM WHERE id=#{id}")
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "description", property = "description"), @Result(column = "name", property = "name"),
            @Result(column = "founding_date", property = "foundingDate"),
            @Result(column = "location", property = "location", one = @One(select = "de.ultical.backend.data.mapper.LocationMapper.get") ),
            @Result(column = "id", property = "rosters", many = @Many(select = "de.ultical.backend.data.mapper.RosterMapper.getForTeam") ),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForTeam") ) })
    Team get(int id);

    @Override
    @Select("SELECT * FROM TEAM")
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "description", property = "description"), @Result(column = "name", property = "name"),
            @Result(column = "founding_date", property = "foundingDate"),
            @Result(column = "location", property = "location", one = @One(select = "de.ultical.backend.data.mapper.LocationMapper.get") ),
            @Result(column = "id", property = "rosters", many = @Many(select = "de.ultical.backend.data.mapper.RosterMapper.getForTeam") ),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForTeam") ) })
    List<Team> getAll();

    // returns all teams the corresponding user is admin of
    @Select("SELECT * FROM TEAM t LEFT JOIN TEAM_ULTICAL_USERS tuc ON tuc.team = t.id WHERE tuc.admin = #{userId}")
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "description", property = "description"), @Result(column = "name", property = "name"),
            @Result(column = "founding_date", property = "foundingDate"),
            @Result(column = "location", property = "location", one = @One(select = "de.ultical.backend.data.mapper.LocationMapper.get") ),
            @Result(column = "id", property = "rosters", many = @Many(select = "de.ultical.backend.data.mapper.RosterMapper.getForTeam") ),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForTeam") ) })
    List<Team> getByUser(int userId);
}
