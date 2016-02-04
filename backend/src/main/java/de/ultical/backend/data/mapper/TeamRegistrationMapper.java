/**
 * Copyright (C) 2015-2016 ultical contributors
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 * * Apache License, version 2.0
 * * Apache Software License, version 1.0
 * * Mozilla Public License, versions 1.0, 1.1 and 2.0
 * * Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU Affero General Public License
 * version 3 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the  GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import de.ultical.backend.model.DivisionRegistrationTeams;
import de.ultical.backend.model.Team;
import de.ultical.backend.model.TeamRegistration;

public interface TeamRegistrationMapper extends BaseMapper<TeamRegistration> {

    // INSERT
    @InsertProvider(type = TeamRegistrationInsertProvider.class, method = "getInsertSql")
    Integer insertAtEnd(@Param("div") DivisionRegistrationTeams div, @Param("team") TeamRegistration reg);

    @Override
    @Insert({ "INSERT INTO TEAM_REGISTRATION",
            "(team, sequence, comment, standing, spirit_score, paid, status, not_qualified)",
            "VALUES (#{team.id}, #{sequence, jdbcType=INTEGER}, #{comment, jdbcType=VARCHAR}, #{standing, jdbcType=INTEGER}, #{spiritScore, jdbcType=REAL}, #{paid}, #{status, jdbcType=VARCHAR}, #{notQualified})" })
    Integer insert(TeamRegistration entity);

    // UPDATE
    @Override
    @Update({ "UPDATE TEAM_REGISTRATION",
            "SET version=version+1, sequence=#{sequence, jdbcType=INTEGER}, comment=#{comment, jdbcType=VARCHAR}, standing=#{standing, jdbcType=INTEGER}, spirit_score=#{spiritScore, jdbcType=REAL}, paid=#{paid}, status=#{status, jdbcType=VARCHAR}, not_qualified=#{notQualified}",
            "WHERE version=#{version} AND id=#{id}" })
    Integer update(TeamRegistration entity);

    // DELETE
    @Delete("DELETE FROM TEAM_REGISTRATION WHERE division_registration=#{div.id} AND team=#{team.id}")
    void delete(@Param("div") DivisionRegistrationTeams div, @Param("team") Team team);

    @Delete("DELETE FROM TEAM_REGISTRATION WHERE division_registration=#{id}")
    void deleteAll(DivisionRegistrationTeams div);

    // SELECT
    @Select("SELECT id, version, team, time_registered, comment, sequence, standing, spirit_score, paid, status, not_qualified FROM TEAM_REGISTRATION WHERE division_registration=#{divId} ORDER BY sequence ASC, time_registered ASC")
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "time_registered", property = "timeRegistered"),
            @Result(column = "team", property = "team", one = @One(select = "de.ultical.backend.data.mapper.TeamMapper.get") , javaType = Team.class),
            @Result(column = "sequence", property = "sequence"), @Result(column = "standing", property = "standing"),
            @Result(column = "spirit_score", property = "spiritScore"), @Result(column = "paid", property = "paid"),
            @Result(column = "status", property = "status"),
            @Result(column = "not_qualified", property = "notQualified"),
            @Result(column = "comment", property = "comment") })
    List<TeamRegistration> getRegistrationsForDivision(int divId);
}
