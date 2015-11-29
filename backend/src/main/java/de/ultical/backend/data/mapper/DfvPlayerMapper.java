package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;

import de.ultical.backend.model.DfvPlayer;

public interface DfvPlayerMapper extends BaseMapper<DfvPlayer> {

	@Override
	@Select("SELECT id, version, dfv_number as dfvNumber, first_name as firstName, last_name as lastName, gender, biography, birth_date as birthDate from DFV_PLAYER WHERE id = #{pk}")
	@ResultType(value=DfvPlayer.class)
	DfvPlayer get(int pk);
	
	@Select("SELECT * from DFV_PLAYER")
	List<DfvPlayer> getAll();
	
	@Override
	@Insert("INSERT INTO DFV_PLAYER (first_name, last_name, dfv_number, gender, birth_date, biography) VALUES (#{firstName},#{lastName},#{dfvNumber},#{gender},#{birthDate},#{biography})")
	Integer insert(DfvPlayer entity);
	
	@Override
	@Update("UPDATE DFV_PLAYER SET version = version + 1, dfv_number = #{dfvNumber}, biography = #{biography}, birth_date = #{birthDate}, first_name = #{firstName}, last_name = #{lastName}, gender = #{gender} WHERE id = #{id} AND version = #{version}")
	Integer update(DfvPlayer entity);
}
