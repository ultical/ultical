package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;

import de.ultical.backend.model.*;

public interface TeamRegistrationMapper {

	@Select("SELECT team, time_registered, comment FROM TEAM_REGISTRATION WHERE division_registration=#{divId} ORDER BY sequence")
	@Results({ @Result(column = "time_registered", property = "timeRegistered"),
			@Result(column = "team", property = "team", one = @One(select = "de.ultical.backend.data.mapper.TeamMapper.get") , javaType = Team.class),
			@Result(column = "comment", property = "comment") })
	List<TeamRegistration> getRegistrationsForDivision(int divId);

	@Delete("DELETE FROM TEAM_REGISTRATION WHERE division_registration=#{div.id} AND team=#{team.id}")
	void delete(@Param("div") DivisionRegistrationTeams div, @Param("team") Team team);

	@Delete("DELETE FROM TEAM_REGISTRATION WHERE division_registration=#{id}")
	void deleteAll(DivisionRegistrationTeams div);

	@InsertProvider(type = TeamRegistrationInsertProvider.class, method = "getInsertSql")
	Integer insertAtEnd(@Param("div") DivisionRegistrationTeams div, @Param("team") Team reg);
}
