package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;

import de.ultical.backend.model.UnregisteredPlayer;

public interface UnregisteredPlayerMapper extends BaseMapper<UnregisteredPlayer> {
	@Select({ "SELECT id, version, first_name as firstName, last_name as lastName, email, gender",
			"FROM UNREGISTERED_PLAYER WHERE id=#{id}" })
	UnregisteredPlayer get(int id);

	@Select({ "SELECT id, version, first_name as firstName, last_name as lastName, email, gender",
			"FROM UNREGISTERED_PLAYER" })
	List<UnregisteredPlayer> getAll();

	@Insert({ "INSERT INTO UNREGISTERED_PLAYER", "(player_id, email)", "VALUES", "(#{id},#{email})" })
	Integer insert(UnregisteredPlayer entity);

	@Update({ "UPDATE UNREGISTERED_PLAYER SET",
			"email=#{email}", "WHERE player_id=#{id}" })
	Integer update(UnregisteredPlayer entity);

	@Delete("DELETE FROM PLAYER WHERE id=#{id}")
	void delete(UnregisteredPlayer entity);
}
