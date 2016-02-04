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
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.mapping.FetchType;

import de.ultical.backend.model.TournamentFormat;
import de.ultical.backend.model.User;

public interface TournamentFormatMapper extends BaseMapper<TournamentFormat> {

    // INSERT
    @Override
    @Insert("INSERT INTO TOURNAMENT_FORMAT (name, description, url, association) VALUES (#{name, jdbcType=VARCHAR},#{description, jdbcType=VARCHAR},#{url, jdbcType=VARCHAR},#{association.id, jdbcType=INTEGER})")
    @Options(keyProperty = "id", useGeneratedKeys = true)
    public Integer insert(TournamentFormat entity);

    @Insert("INSERT INTO TOURNAMENT_FORMAT_ULTICAL_USERS (tournament_format, admin) VALUES (#{tf.id},#{user.id})")
    public Integer insertAdmin(@Param("tf") TournamentFormat tf, @Param("user") User user);

    // UPDATE
    @Override
    @Update({ "UPDATE TOURNAMENT_FORMAT",
            "SET version=version+1, name=#{name, jdbcType=VARCHAR}, description=#{description, jdbcType=VARCHAR}, url=#{url, jdbcType=VARCHAR}, association=#{association.id, jdbcType=INTEGER}",
            "WHERE version=#{version} AND id=#{id}" })
    public Integer update(TournamentFormat entity);

    // DELETE
    @Override
    @Delete("DELETE FROM TOURNAMENT_FORMAT WHERE id=#{id}")
    public void delete(TournamentFormat entity);

    @Delete("DELETE FROM TOURNAMENT_FORMAT_ULTICAL_USERS WHERE tournament_format=#{tf.id} AND admin=#{user.id}")
    public void deleteAdmin(@Param("tf") TournamentFormat tf, @Param("user") User user);

    // SELECT
    @Override
    @Select({ "SELECT id, version, name, url, description, association FROM", "TOURNAMENT_FORMAT", "WHERE id = #{id}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "name", property = "name"), @Result(column = "url", property = "url"),
            @Result(column = "description", property = "description"),
            @Result(column = "association", property = "association", one = @One(select = "de.ultical.backend.data.mapper.AssociationMapper.get") ),
            @Result(column = "id", property = "editions", many = @Many(select = "de.ultical.backend.data.mapper.TournamentEditionMapper.getEditionsForFormat", fetchType = FetchType.EAGER) ),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForFormat") ) })
    TournamentFormat get(int id);

    @Select({ "SELECT id, version, name, url, description, association FROM", "TOURNAMENT_FORMAT", "WHERE id = #{id}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "name", property = "name"), @Result(column = "url", property = "url"),
            @Result(column = "description", property = "description"),
            @Result(column = "association", property = "association", one = @One(select = "de.ultical.backend.data.mapper.AssociationMapper.get") ),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForFormat") ) })
    TournamentFormat getForEdition(int id);

    @Override
    @Select({ "SELECT id, version, name, description, url FROM", "TOURNAMENT_FORMAT" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "name", property = "name"), @Result(column = "url", property = "url"),
            @Result(column = "description", property = "description"),
            @Result(column = "association", property = "association", one = @One(select = "de.ultical.backend.data.mapper.AssociationMapper.get") ),
            @Result(column = "id", property = "editions", many = @Many(select = "de.ultical.backend.data.mapper.TournamentEditionMapper.getEditionsForFormat", fetchType = FetchType.EAGER) ) })
    public List<TournamentFormat> getAll();

    /*
     * get format by event id
     */
    @Select({ "SELECT tf.* FROM TOURNAMENT_FORMAT tf",
            "JOIN TOURNAMENT_EDITION te ON te.tournament_format = tf.id JOIN EVENT e ON e.tournament_edition = te.id",
            "WHERE e.id = #{eventId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "name", property = "name"), @Result(column = "url", property = "url"),
            @Result(column = "description", property = "description"),
            @Result(column = "association", property = "association", one = @One(select = "de.ultical.backend.data.mapper.AssociationMapper.get") ),
            @Result(column = "id", property = "editions", many = @Many(select = "de.ultical.backend.data.mapper.TournamentEditionMapper.getEditionsForFormat", fetchType = FetchType.EAGER) ),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForFormat") ) })
    public TournamentFormat getByEvent(@Param("eventId") int eventId);

}
