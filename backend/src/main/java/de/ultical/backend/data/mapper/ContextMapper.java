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

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import de.ultical.backend.model.Context;

public interface ContextMapper extends BaseMapper<Context> {

    // INSERT
    @Override
    @Insert("INSERT INTO CONTEXT (id, name, acronym) VALUES (#{id, jdbcType=INTEGER},#{name, jdbcType=VARCHAR}, #{acronym, jdbcType=VARCHAR})")
    Integer insert(Context entity);

    @Insert("INSERT INTO CONTEXT_ROSTER (context, roster) VALUES (#{contextId}, #{rosterId})")
    Integer addToRoster(@Param("contextId") int contextId, @Param("rosterId") int rosterId);

    @Insert("INSERT INTO CONTEXT_ROSTER (context, tournament_edition) VALUES (#{contextId}, #{editionId})")
    Integer addToEdition(@Param("contextId") int contextId, @Param("editionId") int editionId);

    // UPDATE
    @Override
    @Update("UPDATE CONTEXT SET version = version+1, name=#{name, jdbcType=VARCHAR}, acronym=#{acronym, jdbcType=VARCHAR} WHERE id=#{id}")
    Integer update(Context entity);

    // DELETE
    @Delete("DELETE FROM CONTEXT_ROSTER WHERE roster = #{rosterId}")
    void deleteAllForRoster(@Param("rosterId") int rosterId);

    @Delete("DELETE FROM CONTEXT_TOURNAMENT_EDITION WHERE tournament_edition = #{editionId}")
    void deleteAllForEdition(@Param("editionId") int editionId);

    // SELECT
    @Override
    @Select({ "SELECT id, version, name, acronym FROM CONTEXT", "WHERE id=#{id}" })
    @Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, javaType = Integer.class),
            @Result(column = "version", property = "version", jdbcType = JdbcType.BIGINT, javaType = Integer.class),
            @Result(column = "acronym", property = "acronym", jdbcType = JdbcType.VARCHAR, javaType = String.class),
            @Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR, javaType = String.class) })
    Context get(@Param("id") int id);

    @Override
    @Select("SELECT id, name, acronym FROM CONTEXT")
    @Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, javaType = Integer.class),
            @Result(column = "version", property = "version", jdbcType = JdbcType.BIGINT, javaType = Integer.class),
            @Result(column = "acronym", property = "acronym", jdbcType = JdbcType.VARCHAR, javaType = String.class),
            @Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR, javaType = String.class) })
    List<Context> getAll();

    @Select({ "SELECT id FROM CONTEXT" })
    Set<Integer> getAllIds();
}
