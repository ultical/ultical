package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;

import de.ultical.backend.model.User;

public interface UserMapper extends BaseMapper<User> {

	@Insert("INSERT INTO ULTICAL_USER (user_name, password, email, dfv_player) VALUES (#{username},#{password},#{email},#{dfvPlayer.id})")	
	Integer insert(User entity);

	@Update({"UPDATE ULTICAL_USER SET version = version + 1, user_name = #{username}, password = #{password}, email=#{email}, dfv_player=#{dfvPlayer.id}",
		"WHERE version = #{version} AND id = #{id}"
	})
	Integer update(User entity);

	@Select({ "SELECT id, user_name, email, password, version, dfv_player", "FROM ULTICAL_USER", "WHERE id = #{id}" })
	@Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
			@Result(column = "user_name", property = "username"), @Result(column = "email", property = "email"),
			@Result(column = "password", property = "password"),
			@Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
	User get(int id);

	@Select({ "SELECT id, user_name, email, password, version, dfv_player", "FROM ULTICAL_USER" })
	@Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
			@Result(column = "user_name", property = "username"), @Result(column = "email", property = "email"),
			@Result(column = "password", property = "password"),
			@Result(column = "dfv_player", property = "dfvPlayer", one = @One(select = "de.ultical.backend.data.mapper.DfvPlayerMapper.get") ) })
	List<User> getAll();

	@Delete("DELETE FROM ULTICAL_USER WHERE id=#{id}")
	void delete(User entity);
}
