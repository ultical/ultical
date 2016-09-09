package de.ultical.backend.data.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import de.ultical.backend.model.Context;

public interface ContextMapper extends BaseMapper<Context> {

    // INSERT
    @Override
    @Insert("INSERT INTO CONTEXT (id, name, acronym) VALUES (#{id, jdbcType=INTEGER},#{name, jdbcType=VARCHAR}, #{acronym, jdbcType=VARCHAR})")
    Integer insert(Context entity);

    @Insert("INSERT INTO CONTEXT_ROSTER (context, roster) VALUES (#{contextId}, #{rosterId})")
    Integer addToRoster(@Param("contextId") int contextId, @Param("rosterId") int rosterId);

    @Insert("INSERT INTO CONTEXT_ROSTER (context, tournament_edition) VALUES (#{contextId}, #{editionId})")
    Integer addToEdition(@Param("contextId") int contextId, @Param("editionId") int editionId);

    // UPDATE
    @Override
    @Update("UPDATE CONTEXT SET version = version+1, name=#{name, jdbcType=VARCHAR}, acronym=#{acronym, jdbcType=VARCHAR} WHERE id=#{id}")
    Integer update(Context entity);

    // DELETE
    @Delete("DELETE FROM CONTEXT_ROSTER WHERE roster = #{rosterId}")
    void deleteAllForRoster(@Param("rosterId") int rosterId);

    @Delete("DELETE FROM CONTEXT_TOURNAMENT_EDITION WHERE tournament_edition = #{editionId}")
    void deleteAllForEdition(@Param("editionId") int editionId);

    // SELECT
    @Override
    @Select({ "SELECT id, version, name, acronym FROM CONTEXT", "WHERE id=#{id}" })
    @Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, javaType = Integer.class),
            @Result(column = "version", property = "version", jdbcType = JdbcType.BIGINT, javaType = Integer.class),
            @Result(column = "acronym", property = "acronym", jdbcType = JdbcType.VARCHAR, javaType = String.class),
            @Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR, javaType = String.class) })
    Context get(@Param("id") int id);

    @Override
    @Select("SELECT id, name, acronym FROM CONTEXT")
    @Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, javaType = Integer.class),
            @Result(column = "version", property = "version", jdbcType = JdbcType.BIGINT, javaType = Integer.class),
            @Result(column = "acronym", property = "acronym", jdbcType = JdbcType.VARCHAR, javaType = String.class),
            @Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR, javaType = String.class) })
    List<Context> getAll();

    @Select({ "SELECT id FROM CONTEXT" })
    Set<Integer> getAllIds();
}
