package de.ultical.backend.data.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import de.ultical.backend.model.Club;

public interface ClubMapper extends BaseMapper<Club> {

    // INSERT
    @Override
    @Insert("INSERT INTO CLUB (id, name, association) VALUES (#{id, jdbcType=INTEGER},#{name, jdbcType=VARCHAR},#{association, jdbcType=INTEGER})")
    Integer insert(Club entity);

    // DELETE
    @Delete("DELETE FROM CLUB WHERE 1")
    void deleteAll();

    // SELECT
    @Override
    @Select({ "SELECT * FROM CLUB", "WHERE id=#{id}" })
    Club get(int id);

}
