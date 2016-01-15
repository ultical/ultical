package de.ultical.backend.data.mapper;

import java.util.Set;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import de.ultical.backend.model.Club;

public interface ClubMapper extends BaseMapper<Club> {

    // INSERT
    @Override
    @Insert("INSERT INTO CLUB (id, name, association) VALUES (#{id, jdbcType=INTEGER},#{name, jdbcType=VARCHAR},#{association, jdbcType=INTEGER})")
    Integer insert(Club entity);

    // UPDATE
    @Override
    @Update("UPDATE CLUB SET name=#{name, jdbcType=VARCHAR}, association=#{association, jdbcType=INTEGER} WHERE id=#{id}")
    Integer update(Club entity);

    // DELETE
    @Delete("DELETE FROM CLUB WHERE 1")
    void deleteAll();

    // SELECT
    @Override
    @Select({ "SELECT id, name, association FROM CLUB", "WHERE id=#{id}" })
    @Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, javaType = Integer.class),
            @Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR, javaType = String.class),
            @Result(column = "association", property = "association", jdbcType = JdbcType.BIGINT, javaType = Integer.class) })
    Club get(@Param("id") int id);

    @Select({ "SELECT id FROM CLUB" })
    Set<Integer> getAllIds();
}
