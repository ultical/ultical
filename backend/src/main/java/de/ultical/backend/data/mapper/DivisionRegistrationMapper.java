package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import de.ultical.backend.model.*;

public interface DivisionRegistrationMapper extends BaseMapper<DivisionRegistration> {

	final String divisionSelect = "SELECT id, version, division_age, division_type, number_of_spots, is_player_registration FROM DIVISION_REGISTRATION";

	@InsertProvider(type = DivisionRegistrationInsertProvider.class, method = "getInsertSql")
	@Options(keyProperty = "reg.id", useGeneratedKeys = true)
	Integer insert(@Param("reg") DivisionRegistration entity, @Param("edition") TournamentEdition edition);

	@Override
	@Select({ divisionSelect, "WHERE id=#{id}" })
	@TypeDiscriminator(column = "is_player_registration", cases = {
			@Case(type = DivisionRegistrationTeams.class, value = "false", results = {
					@Result(column = "id", property = "registeredTeams", javaType = TeamRegistration.class, many = @Many(select = "de.ultical.backend.data.mapper.TeamRegistrationMapper.getRegistrationsForDivision") ) }),
			@Case(type = DivisionRegistrationPlayers.class, value = "true") }, jdbcType = JdbcType.BOOLEAN)
	@Results({ @Result(column = "division_age", property = "divisionAge"),
			@Result(column = "division_type", property = "divisionType"),
			@Result(column = "number_of_spots", property = "numberSpots") })
	DivisionRegistration get(int id);

	@Override
	@Select({ divisionSelect })
	@TypeDiscriminator(column = "is_player_registration", cases = {
			@Case(type = DivisionRegistrationTeams.class, value = "false"),
			@Case(type = DivisionRegistrationPlayers.class, value = "true") }, jdbcType = JdbcType.BOOLEAN)
	@Results({ @Result(column = "division_age", property = "divisionAge"),
			@Result(column = "division_type", property = "divisionType"),
			@Result(column = "number_of_spots", property = "numberSpots") })
	List<DivisionRegistration> getAll();

	@Select({ divisionSelect, "WHERE tournament_edition = #{editionId}" })
	@TypeDiscriminator(column = "is_player_registration", cases = {
			@Case(type = DivisionRegistrationTeams.class, value = "false",results = {
					@Result(column = "id", property = "registeredTeams",  many = @Many(select = "de.ultical.backend.data.mapper.TeamRegistrationMapper.getRegistrationsForDivision") ) }),
			@Case(type = DivisionRegistrationPlayers.class, value = "true") }, jdbcType = JdbcType.BOOLEAN)
	@Results({ @Result(column = "division_age", property = "divisionAge"),
			@Result(column = "division_type", property = "divisionType"),
			@Result(column = "number_of_spots", property = "numberSpots") })
	List<DivisionRegistration> getRegistrationsForEdition(int editionId);

	@Override
	@Update({ "UPDATE DIVISION_REGISTRATION SET version=version+1, division_age=#{divisionAge},",
			"division_type=#{division_type}, number_of_spots=#{numberOfSpots}",
			"WHERE version=#{version} AND id=#{id}" })
	Integer update(DivisionRegistration entity);

	@Delete("DELETE FROM DIVISION_REGISTRATION WHERE id=#{id}")
	void delete(DivisionRegistration reg);
}
