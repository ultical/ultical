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
    @Results({
            @Result(column = "ultical_user", property = "user", one = @One(select = "de.ultical.backend.data.mapper.UserMapper.get") ) })
    MailCode get(String code);

}
