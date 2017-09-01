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
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import de.ultical.backend.model.Resource;

public interface ResourceMapper extends BaseMapper<Resource> {

    final String SELECT_STMT = "SELECT id, version, title, location_path, is_local FROM RESOURCE ";

    // INSERT
    @Override
    @Insert({ "INSERT INTO RESOURCE (title, location_path, is_local, event)",
            "VALUES (#{title}, #{path}, #{local}, #{event.id, jdbcType=INTEGER}" })
    @Options(keyProperty = "id", useGeneratedKeys = true)
    Integer insert(Resource entity);

    // DELETE
    @Override
    @Delete("DELETE FROM RESOURCE WHERE id=#{id}")
    void delete(int id);

    // SELECT
    @Override
    @Select({ SELECT_STMT, "WHERE id=#{id}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "title", property = "title"), @Result(column = "location_path", property = "path"),
            @Result(column = "is_local", property = "local") })
    Resource get(int id);

    @Select({ SELECT_STMT, "WHERE event = #{eventId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "title", property = "title"), @Result(column = "location_path", property = "path"),
            @Result(column = "is_local", property = "local") })
    List<Resource> getForEvent(int eventId);

}
