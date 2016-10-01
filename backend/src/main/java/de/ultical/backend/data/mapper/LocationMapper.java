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
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import de.ultical.backend.model.Location;

public interface LocationMapper extends BaseMapper<Location> {

    final String SELECT_STMT = "SELECT id, version, title, is_main as main, latitude, longitude, city, street, zip_code AS zipCode, country, country_code AS countryCode, additional_info AS additionalInfo";

    // INSERT
    @Override
    @Insert("INSERT INTO LOCATION (title, is_main, latitude, longitude, city, street, zip_code, country, country_code, additional_info) VALUES (#{title, jdbcType=VARCHAR}, #{main, jdbcType=BOOLEAN}, #{latitude, jdbcType=DOUBLE}, #{longitude, jdbcType=DOUBLE}, #{city, jdbcType=VARCHAR}, #{street, jdbcType=VARCHAR}, #{zipCode, jdbcType=VARCHAR}, #{country, jdbcType=VARCHAR}, #{countryCode, jdbcType=VARCHAR}, #{additionalInfo, jdbcType=VARCHAR})")
    @Options(keyProperty = "id", useGeneratedKeys = true)
    Integer insert(Location entity);

    @Insert("INSERT INTO EVENT_LOCATION (event, location) VALUES (#{eventId}, #{locationId})")
    Integer addToEvent(@Param("eventId") int eventId, @Param("locationId") int locationId);

    // UPDATE
    @Override
    @Update("UPDATE LOCATION SET version=version+1, title=#{title, jdbcType=VARCHAR}, is_main=#{main, jdbcType=BOOLEAN}, latitude=#{latitude, jdbcType=DOUBLE}, longitude=#{longitude, jdbcType=DOUBLE}, city=#{city, jdbcType=VARCHAR}, street=#{street, jdbcType=VARCHAR}, zip_code=#{zipCode, jdbcType=VARCHAR}, country=#{country, jdbcType=VARCHAR}, country_code=#{countryCode, jdbcType=VARCHAR}, additional_info=#{additionalInfo, jdbcType=CLOB} WHERE version=#{version} AND id=#{id}")
    Integer update(Location entity);

    // DELETE
    @Override
    @Delete("DELETE FROM LOCATION WHERE id=#{id}")
    void delete(Location entity);

    // SELECT
    @Override
    @Select({ SELECT_STMT, "FROM LOCATION WHERE id=#{id}" })
    Location get(int id);

    @Select({ SELECT_STMT,
            "FROM EVENT_LOCATION el LEFT JOIN LOCATION on el.location = LOCATION.id WHERE el.event=#{eventId}" })
    List<Location> getForEvent(int eventId);

    @Override
    @Select({ SELECT_STMT, "FROM LOCATION" })
    List<Location> getAll();

}
