package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Case;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.TypeDiscriminator;
import org.apache.ibatis.type.JdbcType;

import de.ultical.backend.model.DivisionConfirmation;
import de.ultical.backend.model.DivisionConfirmationPlayers;
import de.ultical.backend.model.DivisionConfirmationTeams;
import de.ultical.backend.model.DivisionRegistration;

public interface DivisionConfirmationMapper extends BaseMapper<DivisionConfirmation> {

    final String divisionSelect = "SELECT dc.id, dc.version, dc.division_registration, dc.event, dr.is_player_registration FROM DIVISION_CONFIRMATION dc LEFT JOIN DIVISION_REGISTRATION dr ON dr.id = dc.division_registration";

    // INSERT
    @Override
    @Insert("INSERT INTO DIVISION_CONFIRMATION (division_registration, event) VALUES (#{divisionRegistration.id}, #{event.id})")
    Integer insert(DivisionConfirmation entity);

    // DELETE
    @Override
    @Delete("DELETE FROM DIVISION_CONFIRMATION WHERE id=#{id}")
    void delete(DivisionConfirmation entity);

    // SELECT
    @Override
    @Select({ divisionSelect, "WHERE dc.id=#{id}" })
    @TypeDiscriminator(column = "is_player_registration", javaType = Boolean.class, cases = {
            @Case(value = "false", type = DivisionConfirmationTeams.class, results = {
                    @Result(column = "id", property = "teams", many = @Many(select = "de.ultical.backend.data.mapper.TeamRegistrationMapper.getRegistrationsForConfirmation") ) }),
            @Case(value = "true", type = DivisionConfirmationPlayers.class) }, jdbcType = JdbcType.BOOLEAN)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "division_registration", property = "divisionRegistration", javaType = DivisionRegistration.class, one = @One(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.get") ) })
    DivisionConfirmation get(int id);

    @Select({ divisionSelect, "WHERE dc.event=#{eventId}" })
    @TypeDiscriminator(column = "is_player_registration", javaType = Boolean.class, cases = {
            @Case(value = "false", type = DivisionConfirmationTeams.class, results = {
                    @Result(column = "id", property = "teams", many = @Many(select = "de.ultical.backend.data.mapper.TeamRegistrationMapper.getRegistrationsForConfirmation") ) }),
            @Case(value = "true", type = DivisionConfirmationPlayers.class) }, jdbcType = JdbcType.BOOLEAN)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "division_registration", property = "divisionRegistration", javaType = DivisionRegistration.class, one = @One(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.get") ) })
    List<DivisionConfirmation> getByEvent(int eventId);
}
