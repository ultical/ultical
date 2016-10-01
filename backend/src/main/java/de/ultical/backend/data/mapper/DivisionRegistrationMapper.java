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
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.TypeDiscriminator;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import de.ultical.backend.model.DivisionRegistration;
import de.ultical.backend.model.DivisionRegistrationPlayers;
import de.ultical.backend.model.DivisionRegistrationTeams;
import de.ultical.backend.model.TournamentEdition;

public interface DivisionRegistrationMapper extends BaseMapper<DivisionRegistration> {

    final String divisionSelect = "SELECT id, version, division_age, division_type, number_of_spots, is_player_registration, division_identifier FROM DIVISION_REGISTRATION";

    // INSERT
    @Insert({ "INSERT INTO DIVISION_REGISTRATION",
            "(tournament_edition, division_type, division_age, number_of_spots, is_player_registration, division_identifier)",
            "VALUES (#{edition.id}, #{divReg.divisionType}, #{divReg.divisionAge}, #{divReg.numberSpots}, #{isPlayerRegistration}, #{divReg.divisionIdentifier, jdbcType=VARCHAR})" })
    @Options(keyProperty = "divReg.id", useGeneratedKeys = true)
    Integer insert(@Param("divReg") DivisionRegistration entity, @Param("edition") TournamentEdition edition,
            @Param("isPlayerRegistration") boolean isPlayerRegistration);

    // UPDATE
    @Override
    @Update({ "UPDATE DIVISION_REGISTRATION SET version=version+1, division_age=#{divisionAge},",
            "division_type=#{division_type}, number_of_spots=#{numberOfSpots}, division_identifier=#{divisionIdentifier, jdbcType=VARCHAR}",
            "WHERE version=#{version} AND id=#{id}" })
    Integer update(DivisionRegistration entity);

    // DELETE
    @Override
    @Delete("DELETE FROM DIVISION_REGISTRATION WHERE id=#{id}")
    void delete(DivisionRegistration reg);

    // SELECT
    @Override
    @Select({ divisionSelect, "WHERE id=#{id}" })
    @TypeDiscriminator(column = "is_player_registration", javaType = Boolean.class, cases = {
            @Case(value = "false", type = DivisionRegistrationTeams.class, results = {
                    @Result(column = "id", property = "registeredTeams", many = @Many(select = "de.ultical.backend.data.mapper.TeamRegistrationMapper.getRegistrationsForDivision")) }),
            @Case(value = "true", type = DivisionRegistrationPlayers.class) }, jdbcType = JdbcType.BOOLEAN)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "division_age", property = "divisionAge"),
            @Result(column = "division_identifier", property = "divisionIdentifier"),
            @Result(column = "division_type", property = "divisionType"),
            @Result(column = "number_of_spots", property = "numberSpots") })
    DivisionRegistration get(int id);

    @Select({ divisionSelect, "WHERE id=#{id}" })
    @TypeDiscriminator(column = "is_player_registration", javaType = Boolean.class, cases = {
            @Case(value = "false", type = DivisionRegistrationTeams.class),
            @Case(value = "true", type = DivisionRegistrationPlayers.class) }, jdbcType = JdbcType.BOOLEAN)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "division_age", property = "divisionAge"),
            @Result(column = "division_identifier", property = "divisionIdentifier"),
            @Result(column = "division_type", property = "divisionType"),
            @Result(column = "number_of_spots", property = "numberSpots") })
    DivisionRegistration getBasic(int id);

    @Override
    @Select({ divisionSelect })
    @TypeDiscriminator(column = "is_player_registration", javaType = Boolean.class, cases = {
            @Case(type = DivisionRegistrationTeams.class, value = "false"),
            @Case(type = DivisionRegistrationPlayers.class, value = "true") }, jdbcType = JdbcType.BOOLEAN)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "division_age", property = "divisionAge"),
            @Result(column = "division_identifier", property = "divisionIdentifier"),
            @Result(column = "division_type", property = "divisionType"),
            @Result(column = "number_of_spots", property = "numberSpots") })
    List<DivisionRegistration> getAll();

    @Select({ divisionSelect, "WHERE tournament_edition = #{editionId}" })
    @TypeDiscriminator(column = "is_player_registration", javaType = Boolean.class, cases = {
            @Case(value = "false", type = DivisionRegistrationTeams.class, results = {
                    @Result(column = "id", property = "registeredTeams", many = @Many(select = "de.ultical.backend.data.mapper.TeamRegistrationMapper.getRegistrationsForDivision")) }),
            @Case(value = "true", type = DivisionRegistrationPlayers.class) }, jdbcType = JdbcType.BOOLEAN)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "division_age", property = "divisionAge"),
            @Result(column = "division_identifier", property = "divisionIdentifier"),
            @Result(column = "division_type", property = "divisionType"),
            @Result(column = "number_of_spots", property = "numberSpots") })
    List<DivisionRegistration> getRegistrationsForEdition(int editionId);

    @Select({ divisionSelect, "WHERE tournament_edition = #{editionId}" })
    @TypeDiscriminator(column = "is_player_registration", javaType = Boolean.class, cases = {
            @Case(value = "false", type = DivisionRegistrationTeams.class),
            @Case(value = "true", type = DivisionRegistrationPlayers.class) }, jdbcType = JdbcType.BOOLEAN)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "division_age", property = "divisionAge"),
            @Result(column = "division_identifier", property = "divisionIdentifier"),
            @Result(column = "division_type", property = "divisionType"),
            @Result(column = "number_of_spots", property = "numberSpots") })
    List<DivisionRegistration> getBasicRegistrationsForEdition(int editionId);
}
