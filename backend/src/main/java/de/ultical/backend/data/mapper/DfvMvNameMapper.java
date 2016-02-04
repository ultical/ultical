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
import org.apache.ibatis.type.JdbcType;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.model.Club;

public interface DfvMvNameMapper {

    final String SELECT_STMT = "SELECT dfv_number as dfvNumber, first_name as firstName, last_name as lastName, dse, club from DFV_MV_NAME";

    // INSERT
    @Insert("INSERT INTO DFV_MV_NAME (dfv_number, first_name, last_name, dse, club) VALUES (#{dfvNumber, jdbcType=INTEGER},#{firstName, jdbcType=VARCHAR},#{lastName, jdbcType=VARCHAR},#{dse},#{club.id, jdbcType=INTEGER})")
    @Options(flushCache = true)
    Integer insert(DfvMvName entity);

    // DELETE
    @Delete("DELETE FROM DFV_MV_NAME WHERE 1=1")
    @Options(flushCache = true)
    void deleteAll();

    // SELECT
    @Select({ SELECT_STMT, "WHERE dfv_number = #{pk}" })
    @Results({ @Result(column = "dfv_number", property = "dfvNumber"),
            @Result(column = "first_name", property = "firstName"),
            @Result(column = "last_name", property = "lastName"), @Result(column = "dse", property = "dse"),
            @Result(column = "club", property = "club", one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get") ) })
    DfvMvName get(int pk);

    @Select(SELECT_STMT)
    @Results({ @Result(column = "dfv_number", property = "dfvNumber"),
            @Result(column = "first_name", property = "firstName"),
            @Result(column = "last_name", property = "lastName"), @Result(column = "dse", property = "dse"),
            @Result(column = "club", property = "club", javaType = Club.class, jdbcType = JdbcType.BIGINT, one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get") ) })
    List<DfvMvName> getAll();

    @Select({ SELECT_STMT, "WHERE first_name = #{firstname} AND last_name = #{lastname}" })
    @Results({ @Result(column = "dfv_number", property = "dfvNumber"),
            @Result(column = "first_name", property = "firstName"),
            @Result(column = "last_name", property = "lastName"), @Result(column = "dse", property = "dse"),
            @Result(column = "club", property = "club", javaType = Club.class, jdbcType = JdbcType.BIGINT, one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get") ) })
    List<DfvMvName> getByName(@Param("firstname") String firstname, @Param("lastname") String lastname);

    @Select({ SELECT_STMT, "WHERE dse=1 AND CONCAT(first_name, ' ', last_name) LIKE #{namePart}" })
    @Results({ @Result(column = "dfv_number", property = "dfvNumber"),
            @Result(column = "first_name", property = "firstName"),
            @Result(column = "last_name", property = "lastName"), @Result(column = "dse", property = "dse"),
            @Result(column = "club", property = "club", javaType = Club.class, jdbcType = JdbcType.BIGINT, one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get") ) })
    List<DfvMvName> find(final String namePart);

}
