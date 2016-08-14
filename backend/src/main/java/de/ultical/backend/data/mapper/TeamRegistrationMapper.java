package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import de.ultical.backend.model.DivisionRegistrationTeams;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.TeamRegistration;

public interface TeamRegistrationMapper extends BaseMapper<TeamRegistration> {

    final String SELECT_STMT = "SELECT id, version, time_registered, comment, sequence, standing, spirit_score, own_spirit_score, paid, status, not_qualified, roster, team_name";

    // INSERT
    @Insert({ "INSERT INTO TEAM_REGISTRATION",
            "(division_registration, sequence, comment, standing, spirit_score, own_spirit_score, paid, status, not_qualified, roster, team_name)",
            "VALUES (#{divisionRegistrationId}, #{teamReg.sequence, jdbcType=INTEGER}, #{teamReg.comment, jdbcType=VARCHAR}, #{teamReg.standing, jdbcType=INTEGER}, #{teamReg.spiritScore, jdbcType=REAL}, #{teamReg.ownSpiritScore, jdbcType=REAL}, #{teamReg.paid}, #{teamReg.status, jdbcType=VARCHAR}, #{teamReg.notQualified}, #{teamReg.roster.id}, #{teamReg.teamName, jdbcType=VARCHAR})" })
    @Options(keyProperty = "teamReg.id", useGeneratedKeys = true)
    Integer insert(@Param("divisionRegistrationId") int divisionRegistrationId,
            @Param("teamReg") TeamRegistration teamReg);

    // UPDATE
    @Override
    @Update({ "UPDATE TEAM_REGISTRATION",
            "SET version=version+1, sequence=#{sequence, jdbcType=INTEGER}, comment=#{comment, jdbcType=VARCHAR}, standing=#{standing, jdbcType=INTEGER}, spirit_score=#{spiritScore, jdbcType=REAL}, own_spirit_score=#{ownSpiritScore, jdbcType=REAL}, paid=#{paid}, status=#{status, jdbcType=VARCHAR}, not_qualified=#{notQualified}",
            "WHERE version=#{version} AND id=#{id}" })
    Integer update(TeamRegistration entity);

    // DELETE
    @Delete("DELETE FROM TEAM_REGISTRATION WHERE division_registration=#{div.id} AND roster=#{roster.id}")
    void delete(@Param("div") DivisionRegistrationTeams div, @Param("roster") Roster roster);

    @Delete("DELETE FROM TEAM_REGISTRATION WHERE division_registration=#{id}")
    void deleteAll(DivisionRegistrationTeams div);

    // SELECT
    @Select({ SELECT_STMT,
            "FROM TEAM_REGISTRATION WHERE division_registration=#{divId} ORDER BY sequence ASC, time_registered ASC" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "time_registered", property = "timeRegistered"),
            @Result(column = "sequence", property = "sequence"), @Result(column = "standing", property = "standing"),
            @Result(column = "spirit_score", property = "spiritScore"),
            @Result(column = "own_spirit_score", property = "ownSpiritScore"),
            @Result(column = "paid", property = "paid"), @Result(column = "status", property = "status"),
            @Result(column = "not_qualified", property = "notQualified"),
            @Result(column = "roster", property = "roster", one = @One(select = "de.ultical.backend.data.mapper.RosterMapper.get"), javaType = Roster.class),
            @Result(column = "team_name", property = "teamName"), @Result(column = "comment", property = "comment") })
    List<TeamRegistration> getRegistrationsForDivision(int divId);

    @Select({ SELECT_STMT, "FROM DIVISION_CONFIRMATION_TEAMS dct",
            "INNER JOIN TEAM_REGISTRATION tr ON dct.team_registration = tr.id",
            "WHERE dct.division_confirmation=#{divId} ORDER BY sequence ASC, time_registered ASC" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "time_registered", property = "timeRegistered"),
            @Result(column = "sequence", property = "sequence"), @Result(column = "standing", property = "standing"),
            @Result(column = "spirit_score", property = "spiritScore"),
            @Result(column = "own_spirit_score", property = "ownSpiritScore"),
            @Result(column = "paid", property = "paid"), @Result(column = "status", property = "status"),
            @Result(column = "not_qualified", property = "notQualified"),
            @Result(column = "roster", property = "roster", one = @One(select = "de.ultical.backend.data.mapper.RosterMapper.get"), javaType = Roster.class),
            @Result(column = "team_name", property = "teamName"), @Result(column = "comment", property = "comment") })
    List<TeamRegistration> getRegistrationsForConfirmation(int divId);

    @Select({ "<script>", SELECT_STMT, "FROM TEAM_REGISTRATION", "WHERE roster IN ",
            "<foreach item='roster' index='index' collection='rosters' open='(' separator=',' close=')'>",
            "#{roster.id}", "</foreach>", "</script>" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "time_registered", property = "timeRegistered"),
            @Result(column = "sequence", property = "sequence"), @Result(column = "standing", property = "standing"),
            @Result(column = "spirit_score", property = "spiritScore"),
            @Result(column = "own_spirit_score", property = "ownSpiritScore"),
            @Result(column = "paid", property = "paid"), @Result(column = "status", property = "status"),
            @Result(column = "not_qualified", property = "notQualified"),
            @Result(column = "team_name", property = "teamName"), @Result(column = "comment", property = "comment") })
    List<TeamRegistration> getByRosters(@Param("rosters") List<Roster> rosters);

    @Select({ SELECT_STMT, "FROM TEAM_REGISTRATION", "WHERE roster= #{roster.id}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "time_registered", property = "timeRegistered"),
            @Result(column = "sequence", property = "sequence"), @Result(column = "standing", property = "standing"),
            @Result(column = "spirit_score", property = "spiritScore"),
            @Result(column = "own_spirit_score", property = "ownSpiritScore"),
            @Result(column = "paid", property = "paid"), @Result(column = "status", property = "status"),
            @Result(column = "not_qualified", property = "notQualified"),
            @Result(column = "team_name", property = "teamName"), @Result(column = "comment", property = "comment") })
    List<TeamRegistration> getByRoster(@Param("roster") Roster roster);
}
