package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import de.ultical.backend.model.TournamentFormat;
import de.ultical.backend.model.User;

public interface TournamentFormatMapper extends BaseMapper<TournamentFormat> {

	@Override
	@Select({ "SELECT id, version, name, description FROM", "TOURNAMENT_FORMAT", "WHERE id = #{id}" })
	@Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
			@Result(column = "name", property = "name"), @Result(column = "description", property = "description"),
			@Result(column = "id", property = "editions", many = @Many(select = "de.ultical.backend.data.mapper.TournamentEditionMapper.getEditionsForFormat", fetchType = FetchType.EAGER) ),
			@Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForFormat") ) })
	TournamentFormat get(int id);

	@Override
	@Update({ "UPDATE TOURNAMENT_FORMAT SET version=version+1, name=#{name}, description=#{description}",
			"WHERE version=#{version} AND id=#{id}" })
	public Integer update(TournamentFormat entity);

	@Override
	@Insert("INSERT INTO TOURNAMENT_FORMAT (name, description) VALUES (#{name},#{description})")
	@Options(keyProperty = "id", useGeneratedKeys = true)
	public Integer insert(TournamentFormat entity);

	@Override
	@Delete("DELETE FROM TOURNAMENT_FORMAT WHERE id=#{id}")
	public void delete(TournamentFormat entity);

	@Override
	@Select({ "SELECT id, version, name, description FROM", "TOURNAMENT_FORMAT" })
	@Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
			@Result(column = "name", property = "name"), @Result(column = "description", property = "description"),
			@Result(column = "id", property = "editions", many = @Many(select = "de.ultical.backend.data.mapper.TournamentEditionMapper.getEditionsForFormat", fetchType = FetchType.EAGER) ) })
	public List<TournamentFormat> getAll();
	
	@Insert("INSERT INTO TOURNAMENT_FORMAT_ULTICAL_USERS (tournament_format, admin) VALUES (#{tf.id},#{user.id})")
	public Integer insertAdmin(@Param("tf") TournamentFormat tf, @Param("user") User user);
	
	@Delete("DELETE FROM TOURNAMENT_FORMAT_ULTICAL_USERS WHERE tournament_format=#{tf.id} AND admin=#{user.id}")
	public void deleteAdmin(@Param("tf") TournamentFormat tf, @Param("user") User user);
}
