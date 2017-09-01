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
import java.util.Set;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import de.ultical.backend.model.Association;
import de.ultical.backend.model.TournamentFormat;
import de.ultical.backend.model.User;

public interface AssociationMapper extends BaseMapper<Association> {

    // INSERT
    @Override
    @Insert("INSERT INTO ASSOCIATION (id, name, acronym, contact) VALUES (#{id, jdbcType=INTEGER},#{name, jdbcType=VARCHAR},#{acronym, jdbcType=VARCHAR},#{contact.id, jdbcType=INTEGER})")
    Integer insert(Association entity);

    @Insert("INSERT INTO ASSOCIATION_ULTICAL_USERS (association, admin) VALUES (#{association.id},#{user.id})")
    public Integer insertAdmin(@Param("association") TournamentFormat association, @Param("user") User user);

    // UPDATE
    @Override
    @Update("UPDATE ASSOCIATION SET name=#{name, jdbcType=VARCHAR}, acronym=#{acronym, jdbcType=VARCHAR}, contact=#{contact.id, jdbcType=INTEGER} WHERE id=#{id}")
    Integer update(Association entity);

    @Update("UPDATE ASSOCIATION SET name=#{name, jdbcType=VARCHAR} WHERE id=#{id}")
    Integer updateBasics(Association entity);

    // SELECT
    @Override
    @Select({ "SELECT id, name, acronym, contact FROM ASSOCIATION", "WHERE id=#{id}" })
    @Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, javaType = Integer.class),
            @Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR, javaType = String.class),
            @Result(column = "acronym", property = "acronym", jdbcType = JdbcType.VARCHAR, javaType = String.class),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForAssociation") ),
            @Result(column = "contact", property = "contact", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get") ) })
    Association get(@Param("id") int id);

    @Override
    @Select("SELECT id, name, contact FROM ASSOCIATION")
    @Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, javaType = Integer.class),
            @Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR, javaType = String.class),
            @Result(column = "acronym", property = "acronym", jdbcType = JdbcType.VARCHAR, javaType = String.class),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForAssociation") ),
            @Result(column = "contact", property = "contact", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get") ) })
    List<Association> getAll();

    @Select({ "SELECT id FROM ASSOCIATION" })
    Set<Integer> getAllIds();
}
