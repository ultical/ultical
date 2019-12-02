package de.ultical.backend.data.mapper;

import de.ultical.backend.model.ContactType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import de.ultical.backend.model.Contact;

import java.util.List;

public interface ContactMapper extends BaseMapper<Contact> {

    final String SELECT_STMT = "SELECT id, version, email, name, phone FROM CONTACT";

    // INSERT
    @Override
    @Insert("INSERT INTO CONTACT (email, name, phone, type) VALUES (#{email, jdbcType=VARCHAR},#{name, jdbcType=VARCHAR},#{phone, jdbcType=VARCHAR},#{type, jdbcType=VARCHAR})")
    @Options(keyProperty = "id", useGeneratedKeys = true)
    Integer insert(Contact entity);

    // UPDATE
    @Override
    @Update({"UPDATE CONTACT SET version=version+1, email=#{email, jdbcType=VARCHAR}, name=#{name, jdbcType=VARCHAR}, phone=#{phone, jdbcType=VARCHAR}, type=#{type, jdbcType=VARCHAR}",
            "WHERE id=#{id}"})
    Integer update(Contact entity);

    // DELETE
    @Override
    @Delete("DELETE FROM CONTACT WHERE id=#{id}")
    void delete(int id);

    // SELECT
    @Override
    @Select({ SELECT_STMT, "WHERE id=#{id}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"), @Result(column = "name", property = "name"),
            @Result(column = "phone", property = "phone") })
    Contact get(int id);

    @Select( {SELECT_STMT, "WHERE type=#{type}"})
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"), @Result(column = "name", property = "name"),
            @Result(column = "phone", property = "phone") })
    List<Contact> getBy(ContactType type);
}
