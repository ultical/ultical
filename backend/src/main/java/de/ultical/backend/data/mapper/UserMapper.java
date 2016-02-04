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
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import de.ultical.backend.model.User;

public interface UserMapper extends BaseMapper<User> {

    // INSERT
    @Override
    @Insert("INSERT INTO ULTICAL_USER (password, email, dfv_player, email_confirmed, dfv_email_opt_in) VALUES (#{password},#{email},#{dfvPlayer.id},#{emailConfirmed},#{dfvEmailOptIn})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer insert(User entity);

    // UPDATE
    @Override
    @Update({
            "UPDATE ULTICAL_USER SET version = version + 1, password = #{password}, email = #{email}, dfv_player = #{dfvPlayer.id}, email_confirmed = #{emailConfirmed}, dfv_email_opt_in = #{dfvEmailOptIn}",
            "WHERE version = #{version} AND id = #{id}" })
    Integer update(User entity);

    @Update({
            "UPDATE ULTICAL_USER SET version = version + 1, email = #{email}, dfv_player = #{dfvPlayer.id}, email_confirmed = #{emailConfirmed}, dfv_email_opt_in = #{dfvEmailOptIn}",
            "WHERE version = #{version} AND id = #{id}" })
    Integer updateWithoutPassword(User entity);

    // DELETE
    @Override
    @Delete("DELETE FROM ULTICAL_USER WHERE id=#{id}")
    void delete(User entity);

    // SELECT
    // without password
    public static final String SELECT_STMT = "SELECT u.id, u.email, u.version, u.dfv_player, u.email_confirmed, u.dfv_email_opt_in";
    // with password
    public static final String SELECT_STMT_FULL = "SELECT u.id, u.email, u.password, u.email_confirmed, u.dfv_email_opt_in, u.version, u.dfv_player";

    @Override
    @Select({ SELECT_STMT, "FROM ULTICAL_USER u", "WHERE u.id = #{id}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"),
            @Result(column = "email_confirmed", property = "emailConfirmed"),
            @Result(column = "dfv_email_opt_in", property = "dfvEmailOptIn"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ), })
    User get(@Param("id") int id);

    @Override
    @Select({ SELECT_STMT, "FROM ULTICAL_USER u" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"),
            @Result(column = "email_confirmed", property = "emailConfirmed"),
            @Result(column = "dfv_email_opt_in", property = "dfvEmailOptIn"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
    List<User> getAll();

    @Select({ SELECT_STMT, "FROM TOURNAMENT_FORMAT_ULTICAL_USERS tfuu LEFT JOIN ULTICAL_USER u", "ON tfuu.admin = u.id",
            "WHERE tfuu.tournament_format = #{formatId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"),
            @Result(column = "email_confirmed", property = "emailConfirmed"),
            @Result(column = "dfv_email_opt_in", property = "dfvEmailOptIn"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
    List<User> getAdminsForFormat(int formatId);

    @Select({ SELECT_STMT, "FROM TEAM_ULTICAL_USERS tuu LEFT JOIN ULTICAL_USER u", "ON tuu.admin = u.id",
            "WHERE tuu.team = #{teamId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"),
            @Result(column = "email_confirmed", property = "emailConfirmed"),
            @Result(column = "dfv_email_opt_in", property = "dfvEmailOptIn"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
    List<User> getAdminsForTeam(int teamId);

    @Select({ SELECT_STMT, "FROM EVENT_ULTICAL_USERS tuu LEFT JOIN ULTICAL_USER u", "ON tuu.admin = u.id",
            "WHERE tuu.event = #{eventId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"),
            @Result(column = "email_confirmed", property = "emailConfirmed"),
            @Result(column = "dfv_email_opt_in", property = "dfvEmailOptIn"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
    List<User> getAdminsForEvent(int eventId);

    @Select({ SELECT_STMT, "FROM ASSOCIATION_ULTICAL_USERS tuu LEFT JOIN ULTICAL_USER u", "ON tuu.admin = u.id",
            "WHERE tuu.association = #{associationId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"),
            @Result(column = "email_confirmed", property = "emailConfirmed"),
            @Result(column = "dfv_email_opt_in", property = "dfvEmailOptIn"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
    List<User> getAdminsForAssociation(int associationId);

    // this is the only query where the password is given out
    @Select({ SELECT_STMT_FULL, "FROM ULTICAL_USER u", "WHERE u.email = #{eMail}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"), @Result(column = "password", property = "password"),
            @Result(column = "email_confirmed", property = "emailConfirmed"),
            @Result(column = "dfv_email_opt_in", property = "dfvEmailOptIn"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
    User getByEmail(final String eMail);

    @Select({ SELECT_STMT, "FROM ULTICAL_USER u", "WHERE u.dfv_player = #{dfvPlayerId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"),
            @Result(column = "email_confirmed", property = "emailConfirmed"),
            @Result(column = "dfv_email_opt_in", property = "dfvEmailOptIn"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
    User getByDfvPlayer(final int dfvPlayerId);

    @Select({ SELECT_STMT,
            "FROM ULTICAL_USER u LEFT JOIN PLAYER p ON u.dfv_player = p.id WHERE CONCAT(p.first_name, ' ', p.last_name) LIKE #{userNamePart}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"),
            @Result(column = "email_confirmed", property = "emailConfirmed"),
            @Result(column = "dfv_email_opt_in", property = "dfvEmailOptIn"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
    List<User> find(final String userNamePart);
}
