package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;

import de.ultical.backend.model.Team;

public interface TeamMapper extends BaseMapper<Team> {

	@Insert("INSERT INTO TEAM (name) VALUES #{name}")
	@Options(keyProperty = "id", useGeneratedKeys = true)
	Integer insert(Team team);

	@Select("SELECT * FROM TEAM WHERE id=#{id}")
	Team get(int id);

	@Select("SELECT * FROM TEAM")
	List<Team> getAll();

	@Update("UPDATE TEAM SET version=version+1, name=#{name} WHERE version=#{version} AND id=#{id}")
	Integer update(Team t);

	@Delete("DELETE FORM TEAM WHERE id=#{id}")
	void delete(Team t);
}
