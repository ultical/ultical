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

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import de.ultical.backend.model.MailCode;

public interface MailCodeMapper {

    final String SELECT_STMT = "SELECT mail_code_type AS type, ultical_user, code FROM MAIL_CODE";

    // INSERT
    @Insert("INSERT INTO MAIL_CODE (mail_code_type, ultical_user, code) VALUES (#{type},#{user.id},#{code, jdbcType=VARCHAR})")
    Integer insert(MailCode entity);

    // DELETE
    @Delete("DELETE FROM MAIL_CODE WHERE code=#{code, jdbcType=VARCHAR}")
    void delete(String code);

    @Delete("DELETE FROM MAIL_CODE WHERE mail_code_type = #{type} AND ultical_user = #{user.id}")
    void deletePreviousEntries(MailCode entity);

    // TODO: put in a job to execute daily(?)...
    @Delete("DELETE FROM MAIL_CODE WHERE type = 'FORGOT_PASSWORD' AND time_created < (NOW() - INTERVAL 3 HOUR)")
    void deleteOldEntries();

    // SELECT
    @Select({ SELECT_STMT,
            "WHERE code = #{code} AND (mail_code_type != 'FORGOT_PASSWORD' OR time_created > (NOW() - INTERVAL 3 HOUR))" })
    @Results({ @Result(column = "mail_code_type", property = "type"),
            @Result(column = "ultical_user", property = "user", one = @One(select = "de.ultical.backend.data.mapper.UserMapper.get") ) })
    MailCode get(String code);

}
