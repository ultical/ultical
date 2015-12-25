package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;

import de.ultical.backend.model.User;

public interface UserMapper extends BaseMapper<User> {

    @Override
    @Insert("INSERT INTO ULTICAL_USER (password, email, dfv_player, email_confirmed, dfv_email_opt_in) VALUES (#{password},#{email},#{dfvPlayer.id},#{emailConfirmed},#{dfvEmailOptIn})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer insert(User entity);

    @Override
    @Update({
            "UPDATE ULTICAL_USER SET version = version + 1, password = #{password}, email = #{email}, dfv_player = #{dfvPlayer.id}, email_confirmed = #{emailConfirmed}, dfv_email_opt_in = #{dfvEmailOptIn}",
            "WHERE version = #{version} AND id = #{id}" })
    Integer update(User entity);

    @Override
    @Select({ "SELECT id, email, password, version, dfv_player, email_confirmed, dfv_email_opt_in", "FROM ULTICAL_USER",
            "WHERE id = #{id}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"), @Result(column = "password", property = "password"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ),
            @Result(column = "email_confirmed", property = "emailConfirmed"),
            @Result(column = "dfv_email_opt_in", property = "dfvEmailOptIn") })
    User get(int id);

    @Override
    @Select({ "SELECT id, email, password, version, dfv_player,  email_confirmed, dfv_email_opt_in",
            "FROM ULTICAL_USER" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"), @Result(column = "password", property = "password"),
            @Result(column = "email_confirmed", property = "emailConfirmed"),
            @Result(column = "dfv_email_opt_in", property = "dfvEmailOptIn"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
    List<User> getAll();

    @Override
    @Delete("DELETE FROM ULTICAL_USER WHERE id=#{id}")
    void delete(User entity);

    @Select({ "SELECT u.id, u.email, u.password, u.email_confirmed, u.dfv_email_opt_in, u.version, u.dfv_player",
            "FROM TOURNAMENT_FORMAT_ULTICAL_USERS tfuu LEFT JOIN ULTICAL_USER u", "ON tfuu.admin = u.id",
            "WHERE tfuu.tournament_format = #{formatId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"), @Result(column = "password", property = "password"),
            @Result(column = "email_confirmed", property = "emailConfirmed"),
            @Result(column = "dfv_email_opt_in", property = "dfvEmailOptIn"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
    List<User> getAdminsForFormat(int formatId);

    @Select({ "SELECT u.id, u.email, u.password, u.email_confirmed, u.dfv_email_opt_in, u.version",
            "FROM ULTICAL_USER u", "WHERE u.email = #{eMail}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"), @Result(column = "password", property = "password"),
            @Result(column = "email_confirmed", property = "emailConfirmed"),
            @Result(column = "dfv_email_opt_in", property = "dfvEmailOptIn") })
    User getUserForEMail(final String eMail);

}
