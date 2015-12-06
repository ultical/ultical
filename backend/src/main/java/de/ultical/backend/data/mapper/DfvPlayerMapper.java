package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;

import de.ultical.backend.model.DfvPlayer;

public interface DfvPlayerMapper extends BaseMapper<DfvPlayer> {
	
	final String SELECT_STMT = "SELECT id, version, dfv_number as dfvNumber, first_name as firstName, last_name as lastName, gender, birth_date as birthDate from PLAYER INNER JOIN DFV_PLAYER ON PLAYER.id = DFV_PLAYER.player_id";
	
	@Override
	@Select({SELECT_STMT,"WHERE id = #{pk} AND is_registered=true"})
	DfvPlayer get(int pk);
	
	@Select(SELECT_STMT)
	List<DfvPlayer> getAll();
	
	@Override
	@Insert("INSERT INTO DFV_PLAYER (player_id, dfv_number, birth_date) VALUES (#{id},#{dfvNumber},#{birthDate})")
	Integer insert(DfvPlayer entity);
	
	@Override
	@Update("UPDATE DFV_PLAYER SET dfv_number = #{dfvNumber}, birth_date = #{birthDate} WHERE player_id = #{id}")
	Integer update(DfvPlayer entity);
}
