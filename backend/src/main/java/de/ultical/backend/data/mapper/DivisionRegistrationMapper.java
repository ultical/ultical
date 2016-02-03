package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Case;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.TypeDiscriminator;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import de.ultical.backend.model.DivisionRegistration;
import de.ultical.backend.model.DivisionRegistrationPlayers;
import de.ultical.backend.model.DivisionRegistrationTeams;
import de.ultical.backend.model.TeamRegistration;
import de.ultical.backend.model.TournamentEdition;

public interface DivisionRegistrationMapper extends BaseMapper<DivisionRegistration> {

    final String divisionSelect = "SELECT id, version, division_age, division_type, number_of_spots, is_player_registration, division_identifier FROM DIVISION_REGISTRATION";

    // INSERT
    @InsertProvider(type = DivisionRegistrationInsertProvider.class, method = "getInsertSql")
    @Options(keyProperty = "reg.id", useGeneratedKeys = true)
    Integer insert(@Param("reg") DivisionRegistration entity, @Param("edition") TournamentEdition edition);

    // UPDATE
    @Override
    @Update({ "UPDATE DIVISION_REGISTRATION SET version=version+1, division_age=#{divisionAge},",
            "division_type=#{division_type}, number_of_spots=#{numberOfSpots}, division_identifier=#{divisionIdentifier, jdbcType=VARCHAR}",
            "WHERE version=#{version} AND id=#{id}" })
    Integer update(DivisionRegistration entity);

    // DELETE
    @Override
    @Delete("DELETE FROM DIVISION_REGISTRATION WHERE id=#{id}")
    void delete(DivisionRegistration reg);

    // SELECT
    @Override
    @Select({ divisionSelect, "WHERE id=#{id}" })
    @TypeDiscriminator(column = "is_player_registration", javaType = Boolean.class, cases = {
            @Case(value = "false", type = DivisionRegistrationTeams.class, results = {
                    @Result(column = "id", property = "registeredTeams", javaType = TeamRegistration.class, many = @Many(select = "de.ultical.backend.data.mapper.TeamRegistrationMapper.getRegistrationsForDivision") ) }),
            @Case(value = "true", type = DivisionRegistrationPlayers.class) }, jdbcType = JdbcType.BOOLEAN)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "division_age", property = "divisionAge"),
            @Result(column = "division_identifier", property = "divisionIdentifier"),
            @Result(column = "division_type", property = "divisionType"),
            @Result(column = "number_of_spots", property = "numberSpots") })
    DivisionRegistration get(int id);

    @Override
    @Select({ divisionSelect })
    @TypeDiscriminator(column = "is_player_registration", javaType = Boolean.class, cases = {
            @Case(type = DivisionRegistrationTeams.class, value = "false"),
            @Case(type = DivisionRegistrationPlayers.class, value = "true") }, jdbcType = JdbcType.BOOLEAN)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "division_age", property = "divisionAge"),
            @Result(column = "division_identifier", property = "divisionIdentifier"),
            @Result(column = "division_type", property = "divisionType"),
            @Result(column = "number_of_spots", property = "numberSpots") })
    List<DivisionRegistration> getAll();

    @Select({ divisionSelect, "WHERE tournament_edition = #{editionId}" })
    @TypeDiscriminator(column = "is_player_registration", javaType = Boolean.class, cases = {
            @Case(value = "false", type = DivisionRegistrationTeams.class, results = {
                    @Result(column = "id", property = "registeredTeams", many = @Many(select = "de.ultical.backend.data.mapper.TeamRegistrationMapper.getRegistrationsForDivision") ) }),
            @Case(value = "true", type = DivisionRegistrationPlayers.class) }, jdbcType = JdbcType.BOOLEAN)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "division_age", property = "divisionAge"),
            @Result(column = "division_identifier", property = "divisionIdentifier"),
            @Result(column = "division_type", property = "divisionType"),
            @Result(column = "number_of_spots", property = "numberSpots") })
    List<DivisionRegistration> getRegistrationsForEdition(int editionId);
}
