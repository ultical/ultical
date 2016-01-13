package de.ultical.backend.data.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import de.ultical.backend.model.Contact;

public interface ContactMapper extends BaseMapper<Contact> {

    final String SELECT_STMT = "SELECT email, name, phone FROM CONTACT";

    // INSERT
    @Override
    @Insert("INSERT INTO CONTACT (email, name, phone) VALUES (#{email},#{name},#{phone})")
    @Options(keyProperty = "id", useGeneratedKeys = true)
    Integer insert(Contact entity);

    // UPDATE
    @Override
    @Update("UPDATE CONTACT SET version=version+1, email=#{email}, name=#{name}, phone=#{phone}")
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

}
