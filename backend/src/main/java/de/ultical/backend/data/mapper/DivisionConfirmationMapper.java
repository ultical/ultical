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

import org.apache.ibatis.annotations.Case;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.TypeDiscriminator;
import org.apache.ibatis.type.JdbcType;

import de.ultical.backend.model.DivisionConfirmation;
import de.ultical.backend.model.DivisionConfirmationPlayers;
import de.ultical.backend.model.DivisionConfirmationTeams;
import de.ultical.backend.model.DivisionRegistration;

public interface DivisionConfirmationMapper extends BaseMapper<DivisionConfirmation> {

    final String divisionSelect = "SELECT dc.id, dc.version, dc.division_registration, dc.event,dc.individual_assignment, dr.is_player_registration FROM DIVISION_CONFIRMATION dc LEFT JOIN DIVISION_REGISTRATION dr ON dr.id = dc.division_registration";

    // INSERT
    @Override
    @Insert("INSERT INTO DIVISION_CONFIRMATION (division_registration, event, individual_assignment) VALUES (#{divisionRegistration.id}, #{event.id}, #{individualAssignment})")
    Integer insert(DivisionConfirmation entity);

    // DELETE
    @Override
    @Delete("DELETE FROM DIVISION_CONFIRMATION WHERE id=#{id}")
    void delete(DivisionConfirmation entity);

    // SELECT
    @Override
    @Select({ divisionSelect, "WHERE dc.id=#{id}" })
    @TypeDiscriminator(column = "is_player_registration", javaType = Boolean.class, cases = {
            @Case(value = "false", type = DivisionConfirmationTeams.class, results = {
                    @Result(column = "id", property = "teams", many = @Many(select = "de.ultical.backend.data.mapper.TeamRegistrationMapper.getRegistrationsForConfirmation")) }),
            @Case(value = "true", type = DivisionConfirmationPlayers.class) }, jdbcType = JdbcType.BOOLEAN)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "individual_assignment", property = "individualAssignment", javaType = Boolean.class, jdbcType = JdbcType.BOOLEAN),
            @Result(column = "division_registration", property = "divisionRegistration", javaType = DivisionRegistration.class, one = @One(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.get")) })
    DivisionConfirmation get(int id);

    @Select({ divisionSelect, "WHERE dc.event=#{eventId}" })
    @TypeDiscriminator(column = "is_player_registration", javaType = Boolean.class, cases = {
            @Case(value = "false", type = DivisionConfirmationTeams.class, results = {
                    @Result(column = "id", property = "teams", many = @Many(select = "de.ultical.backend.data.mapper.TeamRegistrationMapper.getRegistrationsForConfirmation")) }),
            @Case(value = "true", type = DivisionConfirmationPlayers.class) }, jdbcType = JdbcType.BOOLEAN)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "individual_assignment", property = "individualAssignment", javaType = Boolean.class, jdbcType = JdbcType.BOOLEAN),
            @Result(column = "division_registration", property = "divisionRegistration", javaType = DivisionRegistration.class, one = @One(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.get")) })
    List<DivisionConfirmation> getByEvent(int eventId);

    @Select({ divisionSelect, "WHERE dc.event=#{eventId}" })
    @TypeDiscriminator(column = "is_player_registration", javaType = Boolean.class, cases = {
            @Case(value = "false", type = DivisionConfirmationTeams.class),
            @Case(value = "true", type = DivisionConfirmationPlayers.class) }, jdbcType = JdbcType.BOOLEAN)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "individual_assignment", property = "individualAssignment", javaType = Boolean.class, jdbcType = JdbcType.BOOLEAN),
            @Result(column = "division_registration", property = "divisionRegistration", javaType = DivisionRegistration.class, one = @One(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.getBasic")) })
    List<DivisionConfirmation> getBasicsByEvent(int eventId);
}
