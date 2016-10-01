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
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.mapping.FetchType;

import de.ultical.backend.model.Season;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.TournamentFormat;

public interface TournamentEditionMapper extends BaseMapper<TournamentEdition> {

    final String editionSelectBase = "SELECT id, version, tournament_format, name, season, "
            + "registration_start, registration_end, hashtag, organizer, alternative_matchday_name, context, allow_event_team_reg_management "
            + "FROM TOURNAMENT_EDITION";

    // INSERT
    @Override
    @Insert({ "INSERT INTO TOURNAMENT_EDITION",
            "(tournament_format, name, season, registration_start, registration_end, organizer, hashtag, alternative_matchday_name, context, allow_event_team_reg_management)",
            "VALUES (#{tournamentFormat.id, jdbcType=INTEGER},#{name, jdbcType=VARCHAR},#{season.id, jdbcType=INTEGER},",
            "#{registrationStart, jdbcType=DATE},#{registrationEnd, jdbcType=DATE},",
            "#{organizer.id, jdbcType=INTEGER}, #{hashtag, jdbcType=VARCHAR}, #{alternativeMatchdayName, jdbcType=VARCHAR},",
            "#{context.id, jdbcType=INTEGER}, #{allowEventTeamRegManagement})" })
    @Options(keyProperty = "id", useGeneratedKeys = true)
    Integer insert(TournamentEdition entity);

    // UPDATE
    @Override
    @Update({ "UPDATE TOURNAMENT_EDITION SET version=version+1, hashtag=#{hashtag, jdbcType=VARCHAR},",
            "tournament_format=#{tournamentFormat.id, jdbcType=INTEGER}, name=#{name, jdbcType=VARCHAR},",
            "season=#{season.id, jdbcType=INTEGER}, registration_start=#{registrationStart, jdbcType=DATE},",
            "registration_end=#{registrationEnd, jdbcType=DATE}, organizer=#{organizer.id, jdbcType=INTEGER},",
            "alternative_matchday_name=#{alternativeMatchdayName, jdbcType=VARCHAR}, context = #{context.id, jdbcType=INTEGER},",
            "allow_event_team_reg_management=#{allowEventTeamRegManagement}", "WHERE id=#{id} AND version=#{version}" })
    Integer update(TournamentEdition entity);

    // DELETE
    @Override
    @Delete("DELETE FROM TOURNAMENT_EDITION WHERE id=#{id}")
    void delete(TournamentEdition entity);

    // SELECT
    @Override
    @Select({ editionSelectBase, "WHERE id = #{id}" })
    @Results({ @Result(column = "alternative_matchday_name", property = "alternativeMatchdayName"),
            @Result(column = "id", property = "events", many = @Many(select = "de.ultical.backend.data.mapper.EventMapper.getEventsForEdition", fetchType = FetchType.EAGER)),
            @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "tournament_format", property = "tournamentFormat", javaType = TournamentFormat.class, one = @One(select = "de.ultical.backend.data.mapper.TournamentFormatMapper.get")),
            @Result(column = "name", property = "name"),
            @Result(column = "season", property = "season", javaType = Season.class, one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get")),
            @Result(column = "registration_start", property = "registrationStart"),
            @Result(column = "registration_end", property = "registrationEnd"),
            @Result(column = "hashtag", property = "hashtag"),
            @Result(column = "context", property = "context", one = @One(select = "de.ultical.backend.data.mapper.ContextMapper.get")),
            @Result(column = "allow_event_team_reg_management", property = "allowEventTeamRegManagement"),
            @Result(column = "id", property = "fees", many = @Many(select = "de.ultical.backend.data.mapper.FeeMapper.getForTournamentEdition")),
            @Result(column = "organizer", property = "organizer", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get")),
            @Result(column = "id", property = "divisionRegistrations", many = @Many(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.getRegistrationsForEdition")) })
    TournamentEdition get(int id);

    // use this method to get Editions when loading an event - so no event
    // entities should be included (double happiness)
    @Select({ editionSelectBase, "WHERE id = #{id}" })
    @Results({ @Result(column = "alternative_matchday_name", property = "alternativeMatchdayName"),
            @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "tournament_format", property = "tournamentFormat", javaType = TournamentFormat.class, one = @One(select = "de.ultical.backend.data.mapper.TournamentFormatMapper.getForEdition")),
            @Result(column = "name", property = "name"),
            @Result(column = "season", property = "season", javaType = Season.class, one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get")),
            @Result(column = "registration_start", property = "registrationStart"),
            @Result(column = "registration_end", property = "registrationEnd"),
            @Result(column = "hashtag", property = "hashtag"),
            @Result(column = "context", property = "context", one = @One(select = "de.ultical.backend.data.mapper.ContextMapper.get")),
            @Result(column = "allow_event_team_reg_management", property = "allowEventTeamRegManagement"),
            @Result(column = "id", property = "fees", many = @Many(select = "de.ultical.backend.data.mapper.FeeMapper.getForTournamentEdition")),
            @Result(column = "organizer", property = "organizer", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get")),
            @Result(column = "id", property = "divisionRegistrations", many = @Many(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.getRegistrationsForEdition")) })
    TournamentEdition getForEvent(int id);

    @Select({ editionSelectBase, "WHERE id = #{id}" })
    @Results({ @Result(column = "alternative_matchday_name", property = "alternativeMatchdayName"),
            @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "tournament_format", property = "tournamentFormat", javaType = TournamentFormat.class, one = @One(select = "de.ultical.backend.data.mapper.TournamentFormatMapper.getForEdition")),
            @Result(column = "name", property = "name"),
            @Result(column = "season", property = "season", javaType = Season.class, one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get")),
            @Result(column = "registration_start", property = "registrationStart"),
            @Result(column = "registration_end", property = "registrationEnd"),
            @Result(column = "hashtag", property = "hashtag"),
            @Result(column = "context", property = "context", one = @One(select = "de.ultical.backend.data.mapper.ContextMapper.get")),
            @Result(column = "allow_event_team_reg_management", property = "allowEventTeamRegManagement"),
            @Result(column = "id", property = "divisionRegistrations", many = @Many(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.getBasicRegistrationsForEdition")) })
    TournamentEdition getBasicForEvent(int id);

    @Select({ editionSelectBase, "WHERE tournament_format = #{formatId}" })
    @Results({ @Result(column = "alternative_matchday_name", property = "alternativeMatchdayName"),
            @Result(column = "id", property = "events", many = @Many(select = "de.ultical.backend.data.mapper.EventMapper.getEventsForEdition", fetchType = FetchType.EAGER)),
            @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "name", property = "name"),
            @Result(column = "season", property = "season", javaType = Season.class, one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get")),
            @Result(column = "registration_start", property = "registrationStart"),
            @Result(column = "registration_end", property = "registrationEnd"),
            @Result(column = "hashtag", property = "hashtag"),
            @Result(column = "context", property = "context", one = @One(select = "de.ultical.backend.data.mapper.ContextMapper.get")),
            @Result(column = "allow_event_team_reg_management", property = "allowEventTeamRegManagement"),
            @Result(column = "id", property = "fees", many = @Many(select = "de.ultical.backend.data.mapper.FeeMapper.getForTournamentEdition")),
            @Result(column = "organizer", property = "organizer", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get")),
            @Result(column = "id", property = "divisionRegistrations", many = @Many(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.getRegistrationsForEdition")) })
    List<TournamentEdition> getEditionsForFormat(int formatId);

    @Override
    @Select(editionSelectBase)
    @Results({ @Result(column = "alternative_matchday_name", property = "alternativeMatchdayName"),
            @Result(column = "id", property = "events", many = @Many(select = "de.ultical.backend.data.mapper.EventMapper.getEventsForEdition", fetchType = FetchType.EAGER)),
            @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "tournament_format", property = "tournamentFormat", javaType = TournamentFormat.class, one = @One(select = "de.ultical.backend.data.mapper.TournamentFormatMapper.getForEdition")),
            @Result(column = "name", property = "name"),
            @Result(column = "season", property = "season", javaType = Season.class, one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get")),
            @Result(column = "registration_start", property = "registrationStart"),
            @Result(column = "registration_end", property = "registrationEnd"),
            @Result(column = "hashtag", property = "hashtag"),
            @Result(column = "context", property = "context", one = @One(select = "de.ultical.backend.data.mapper.ContextMapper.get")),
            @Result(column = "allow_event_team_reg_management", property = "allowEventTeamRegManagement"),
            @Result(column = "id", property = "fees", many = @Many(select = "de.ultical.backend.data.mapper.FeeMapper.getForTournamentEdition")),
            @Result(column = "organizer", property = "organizer", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get")),
            @Result(column = "id", property = "divisionRegistrations", many = @Many(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.getRegistrationsForEdition")) })
    List<TournamentEdition> getAll();

    @Select({ "SELECT te.* FROM TOURNAMENT_EDITION te",
            "LEFT JOIN DIVISION_REGISTRATION dr ON dr.tournament_edition = te.id",
            "LEFT JOIN TEAM_REGISTRATION tr ON tr.division_registration = dr.id",
            "WHERE tr.id = #{teamRegistrationId}" })
    TournamentEdition getByTeamRegistration(int teamRegistrationId);
}
