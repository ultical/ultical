package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.mapping.FetchType;

import de.ultical.backend.model.TournamentFormat;
import de.ultical.backend.model.User;

public interface TournamentFormatMapper extends BaseMapper<TournamentFormat> {

    // INSERT
    @Override
    @Insert("INSERT INTO TOURNAMENT_FORMAT (name, description, url, association) VALUES (#{name, jdbcType=VARCHAR},#{description, jdbcType=VARCHAR},#{url, jdbcType=VARCHAR},#{association.id, jdbcType=INTEGER})")
    @Options(keyProperty = "id", useGeneratedKeys = true)
    public Integer insert(TournamentFormat entity);

    @Insert("INSERT INTO TOURNAMENT_FORMAT_ULTICAL_USERS (tournament_format, admin) VALUES (#{tf.id},#{user.id})")
    public Integer insertAdmin(@Param("tf") TournamentFormat tf, @Param("user") User user);

    // UPDATE
    @Override
    @Update({ "UPDATE TOURNAMENT_FORMAT",
            "SET version=version+1, name=#{name, jdbcType=VARCHAR}, description=#{description, jdbcType=VARCHAR}, url=#{url, jdbcType=VARCHAR}, association=#{association.id, jdbcType=INTEGER}",
            "WHERE version=#{version} AND id=#{id}" })
    public Integer update(TournamentFormat entity);

    // DELETE
    @Override
    @Delete("DELETE FROM TOURNAMENT_FORMAT WHERE id=#{id}")
    public void delete(TournamentFormat entity);

    @Delete("DELETE FROM TOURNAMENT_FORMAT_ULTICAL_USERS WHERE tournament_format=#{tf.id} AND admin=#{user.id}")
    public void deleteAdmin(@Param("tf") TournamentFormat tf, @Param("user") User user);

    // SELECT
    @Override
    @Select({ "SELECT id, version, name, url, description, association FROM", "TOURNAMENT_FORMAT", "WHERE id = #{id}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "name", property = "name"), @Result(column = "url", property = "url"),
            @Result(column = "description", property = "description"),
            @Result(column = "association", property = "association", one = @One(select = "de.ultical.backend.data.mapper.AssociationMapper.get") ),
            @Result(column = "id", property = "editions", many = @Many(select = "de.ultical.backend.data.mapper.TournamentEditionMapper.getEditionsForFormat", fetchType = FetchType.EAGER) ),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForFormat") ) })
    TournamentFormat get(int id);

    @Select({ "SELECT id, version, name, url, description, dfv_official FROM", "TOURNAMENT_FORMAT",
            "WHERE id = #{id}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "name", property = "name"), @Result(column = "url", property = "url"),
            @Result(column = "description", property = "description"),
            @Result(column = "association", property = "association", one = @One(select = "de.ultical.backend.data.mapper.AssociationMapper.get") ),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForFormat") ) })
    TournamentFormat getForEdition(int id);

    @Override
    @Select({ "SELECT id, version, name, description, url FROM", "TOURNAMENT_FORMAT" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "name", property = "name"), @Result(column = "url", property = "url"),
            @Result(column = "description", property = "description"),
            @Result(column = "association", property = "association", one = @One(select = "de.ultical.backend.data.mapper.AssociationMapper.get") ),
            @Result(column = "id", property = "editions", many = @Many(select = "de.ultical.backend.data.mapper.TournamentEditionMapper.getEditionsForFormat", fetchType = FetchType.EAGER) ) })
    public List<TournamentFormat> getAll();

}
