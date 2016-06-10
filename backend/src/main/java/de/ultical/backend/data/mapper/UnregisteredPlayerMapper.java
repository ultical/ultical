package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import de.ultical.backend.model.UnregisteredPlayer;

public interface UnregisteredPlayerMapper extends BaseMapper<UnregisteredPlayer> {
	@Override
    @Select({ "SELECT id, version, first_name as firstName, last_name as lastName, email, gender",
			"FROM UNREGISTERED_PLAYER WHERE id=#{id}" })
	UnregisteredPlayer get(int id);

	@Override
    @Select({ "SELECT id, version, first_name as firstName, last_name as lastName, email, gender",
			"FROM UNREGISTERED_PLAYER" })
	List<UnregisteredPlayer> getAll();

	@Override
    @Insert({ "INSERT INTO UNREGISTERED_PLAYER", "(player_id, email)", "VALUES", "(#{id},#{email})" })
	Integer insert(UnregisteredPlayer entity);

	@Override
    @Update({ "UPDATE UNREGISTERED_PLAYER SET",
			"email=#{email}", "WHERE player_id=#{id}" })
	Integer update(UnregisteredPlayer entity);

	@Override
    @Delete("DELETE FROM PLAYER WHERE id=#{id}")
	void delete(UnregisteredPlayer entity);
}
