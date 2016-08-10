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
