package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;

import de.ultical.backend.model.User;

public interface UserMapper extends BaseMapper<User> {

	@Insert("INSERT INTO ULTICAL_USER (password, email, dfv_player) VALUES (#{password},#{email},#{dfvPlayer.id})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	Integer insert(User entity);

	@Update({
			"UPDATE ULTICAL_USER SET version = version + 1, password = #{password}, email=#{email}, dfv_player=#{dfvPlayer.id}",
			"WHERE version = #{version} AND id = #{id}" })
	Integer update(User entity);

	@Select({ "SELECT id, email, password, version, dfv_player", "FROM ULTICAL_USER", "WHERE id = #{id}" })
	@Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
			@Result(column = "email", property = "email"),
			@Result(column = "password", property = "password"),
			@Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
	User get(int id);

	@Select({ "SELECT id, email, password, version, dfv_player", "FROM ULTICAL_USER" })
	@Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
			@Result(column = "email", property = "email"),
			@Result(column = "password", property = "password"),
			@Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
	List<User> getAll();

	@Delete("DELETE FROM ULTICAL_USER WHERE id=#{id}")
	void delete(User entity);

	@Select({ "SELECT u.id, u.email, u.password, u.version, u.dfv_player",
			"FROM TOURNAMENT_FORMAT_ULTICAL_USERS tfuu LEFT JOIN ULTICAL_USER u",
			"ON tfuu.admin = u.id",
			"WHERE tfuu.tournament_format = #{formatId}" })
	@Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
		@Result(column = "email", property = "email"),
		@Result(column = "password", property = "password"),
		@Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
	List<User> getAdminsForFormat(int formatId);
}
