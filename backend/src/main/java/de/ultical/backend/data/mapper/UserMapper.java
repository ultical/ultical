package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import de.ultical.backend.model.User;

public interface UserMapper extends BaseMapper<User> {

    // INSERT
    @Override
    @Insert("INSERT INTO ULTICAL_USER (password, email, dfv_player, email_confirmed, dfv_email_opt_in) VALUES (#{password},#{email},#{dfvPlayer.id},#{emailConfirmed},#{dfvEmailOptIn})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer insert(User entity);

    // UPDATE
    @Override
    @Update({
            "UPDATE ULTICAL_USER SET version = version + 1, password = #{password}, email = #{email}, dfv_player = #{dfvPlayer.id}, email_confirmed = #{emailConfirmed}, dfv_email_opt_in = #{dfvEmailOptIn}",
            "WHERE version = #{version} AND id = #{id}" })
    Integer update(User entity);

    // DELETE
    @Override
    @Delete("DELETE FROM ULTICAL_USER WHERE id=#{id}")
    void delete(User entity);

    // SELECT
    // without password
    public static final String SELECT_STMT = "SELECT u.id, u.email, u.version, u.dfv_player";
    // with password
    public static final String SELECT_STMT_FULL = "SELECT u.id, u.email, u.password, u.email_confirmed, u.dfv_email_opt_in, u.version, u.dfv_player";

    @Override
    @Select({ SELECT_STMT, "FROM ULTICAL_USER u", "WHERE u.id = #{id}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ), })
    User get(@Param("id") int id);

    @Override
    @Select({ SELECT_STMT, "FROM ULTICAL_USER u" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
    List<User> getAll();

    @Select({ SELECT_STMT, "FROM TOURNAMENT_FORMAT_ULTICAL_USERS tfuu LEFT JOIN ULTICAL_USER u", "ON tfuu.admin = u.id",
            "WHERE tfuu.tournament_format = #{formatId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
    List<User> getAdminsForFormat(int formatId);

    @Select({ SELECT_STMT, "FROM TEAM_ULTICAL_USERS tuu LEFT JOIN ULTICAL_USER u", "ON tuu.admin = u.id",
            "WHERE tuu.team = #{teamId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
    List<User> getAdminsForTeam(int teamId);

    // this is the only query where the password is given out
    @Select({ SELECT_STMT_FULL, "FROM ULTICAL_USER u", "WHERE u.email = #{eMail}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"), @Result(column = "password", property = "password"),
            @Result(column = "email_confirmed", property = "emailConfirmed"),
            @Result(column = "dfv_email_opt_in", property = "dfvEmailOptIn"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
    User getByEmail(final String eMail);

    @Select({ SELECT_STMT, "FROM ULTICAL_USER u", "WHERE u.dfv_player = #{dfvPlayerId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
    User getByDfvPlayer(final int dfvPlayerId);

    @Select({ SELECT_STMT,
            "FROM ULTICAL_USER u LEFT JOIN PLAYER p ON u.dfv_player = p.id WHERE CONCAT(p.first_name, ' ', p.last_name) LIKE #{userNamePart}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "email", property = "email"),
            @Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
    List<User> find(final String userNamePart);
}
