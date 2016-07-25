package de.ultical.backend.data.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.Player;
import de.ultical.backend.model.Roster;

public interface RosterMapper extends BaseMapper<Roster> {

    final String SELECT_STMT = "SELECT id, version, team, season, division_age as divisionAge, division_type as divisionType, name_addition as nameAddition, context FROM ROSTER";

    // INSERT
    @Override
    @Insert("INSERT INTO ROSTER (team, season, division_age, division_type, name_addition, context) VALUES (#{team.id},#{season.id},#{divisionAge},#{divisionType},#{nameAddition, jdbcType=VARCHAR},#{context.id, jdbcType=INTEGER})")
    @Options(keyProperty = "id", useGeneratedKeys = true)
    Integer insert(Roster entity);

    @Insert("INSERT INTO ROSTER_PLAYERS (roster, player) VALUES (#{roster.id}, #{player.id})")
    Integer addPlayer(@Param("roster") Roster roster, @Param("player") Player player);

    // UPDATE
    @Override
    @Update({
            "UPDATE ROSTER SET version = version + 1, team = #{team.id}, season = #{season.id}, division_age = #{divisionAge}, division_type = #{divisionType},",
            "name_addition = #{nameAddition, jdbcType=VARCHAR}, context = #{context.id, jdbcType=INTEGER}",
            "WHERE id = #{id} AND version = #{version}" })
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
            @Result(column = "name_addition", property = "nameAddition"),
            @Result(column = "context", property = "context", one = @One(select = "de.ultical.backend.data.mapper.ContextMapper.get") ),
            @Result(column = "team", property = "team", one = @One(select = "de.ultical.backend.data.mapper.TeamMapper.getForRoster") ),
            @Result(column = "season", property = "season", one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
            @Result(column = "id", property = "players", many = @Many(select = "de.ultical.backend.data.mapper.RosterPlayerMapper.getByRoster") ) })
    Roster get(int id);

    @Override
    @Select(SELECT_STMT)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "division_age", property = "division_age"),
            @Result(column = "division_type", property = "division_type"),
            @Result(column = "name_addition", property = "nameAddition"),
            @Result(column = "context", property = "context", one = @One(select = "de.ultical.backend.data.mapper.ContextMapper.get") ),
            @Result(column = "season", property = "season", one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
            @Result(column = "id", property = "players", many = @Many(select = "de.ultical.backend.data.mapper.RosterPlayerMapper.getByRoster") ) })
    List<Roster> getAll();

    @Select({ SELECT_STMT, "WHERE team = #{teamId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "division_age", property = "division_age"),
            @Result(column = "division_type", property = "division_type"),
            @Result(column = "name_addition", property = "nameAddition"),
            @Result(column = "context", property = "context", one = @One(select = "de.ultical.backend.data.mapper.ContextMapper.get") ),
            @Result(column = "season", property = "season", one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
            @Result(column = "id", property = "players", many = @Many(select = "de.ultical.backend.data.mapper.RosterPlayerMapper.getByRoster") ) })
    List<Roster> getForTeam(Integer teamId);

    // get roster of a specific team in one season to check for
    // roster uniqueness
    @Select({ "<script>", SELECT_STMT,
            "WHERE team = #{roster.team.id} AND season = #{roster.season.id} AND division_age = #{roster.divisionAge} AND division_type = #{roster.divisionType}",
            "AND name_addition = #{roster.nameAddition}", "AND <choose><when test='roster.context == null'>",
            "context IS NULL", "</when>", "<otherwise>", "context = #{roster.context.id}", "</otherwise>", "</choose>",
            "</script>" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version") })
    Roster getByTeamSeasonDivision(@Param("roster") Roster roster);

    // get roster that contains specific player in one season/division/context
    @Select({ "<script>", SELECT_STMT, "r LEFT JOIN ROSTER_PLAYERS rp ON r.id = rp.roster",
            "WHERE rp.player = #{playerId} AND r.season = #{roster.season.id} AND r.division_age = #{roster.divisionAge} AND r.division_type = #{roster.divisionType} AND ",
            "<choose><when test='roster.context == null'>", "context IS NULL", "</when>", "<otherwise>",
            "context = #{roster.context.id}", "</otherwise>", "</choose>", "</script>" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "division_age", property = "division_age"),
            @Result(column = "division_type", property = "division_type"),
            @Result(column = "name_addition", property = "nameAddition"),
            @Result(column = "context", property = "context", one = @One(select = "de.ultical.backend.data.mapper.ContextMapper.get") ),
            @Result(column = "team", property = "team", one = @One(select = "de.ultical.backend.data.mapper.TeamMapper.getForRoster") ),
            @Result(column = "season", property = "season", one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
            @Result(column = "id", property = "players", many = @Many(select = "de.ultical.backend.data.mapper.RosterPlayerMapper.getByRoster") ) })
    List<Roster> getByPlayerSeasonDivision(@Param("playerId") Integer playerId, @Param("roster") Roster roster);

    // get blocking date for roster
    @Select({ "SELECT e.start_date AS blockingDate FROM EVENT e",
            "JOIN TOURNAMENT_EDITION te ON e.tournament_edition = te.id",
            "JOIN DIVISION_REGISTRATION dr ON dr.tournament_edition = te.id",
            "JOIN TEAM_REGISTRATION tr ON tr.division_registration = dr.id", "JOIN ROSTER r ON tr.roster = r.id",
            "WHERE tr.status = 'CONFIRMED' AND tr.not_qualified = false AND r.id = #{rosterId}" })
    List<LocalDate> getBlockingDate(int rosterId);

    @Select({ SELECT_STMT, " r LEFT JOIN ROSTER_PLAYERS rp ON r.id = rp.roster", " WHERE rp.player = #{id}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "division_age", property = "division_age"),
            @Result(column = "division_type", property = "division_type"),
            @Result(column = "name_addition", property = "nameAddition"),
            @Result(column = "context", property = "context", one = @One(select = "de.ultical.backend.data.mapper.ContextMapper.get") ),
            @Result(column = "team", property = "team", one = @One(select = "de.ultical.backend.data.mapper.TeamMapper.getForRoster") ),
            @Result(column = "season", property = "season", one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
            @Result(column = "id", property = "players", many = @Many(select = "de.ultical.backend.data.mapper.RosterPlayerMapper.getByRoster") ) })
    List<Roster> getRostersForPlayer(DfvPlayer player);
}
