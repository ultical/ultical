package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.One;
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
    Integer insert(DfvMvName entity);

    // DELETE
    @Delete("DELETE FROM DFV_MV_NAME WHERE 1=1")
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

    // this commented out version does not match aous to äöüß
    // @Select({ SELECT_STMT, "WHERE dse=1 AND CONCAT(first_name, ' ',last_name)
    // LIKE #{namePart}" })
    @Select({ SELECT_STMT, "WHERE dse=1",
            "AND (CONCAT(first_name, ' ', last_name) LIKE #{namePart} OR CONCAT(first_name, ' ', last_name) LIKE _utf8 #{namePart} COLLATE utf8_general_ci)" })
    @Results({ @Result(column = "dfv_number", property = "dfvNumber"),
            @Result(column = "first_name", property = "firstName"),
            @Result(column = "last_name", property = "lastName"), @Result(column = "dse", property = "dse"),
            @Result(column = "club", property = "club", javaType = Club.class, jdbcType = JdbcType.BIGINT, one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get") ) })
    List<DfvMvName> find(final String namePart);

}
