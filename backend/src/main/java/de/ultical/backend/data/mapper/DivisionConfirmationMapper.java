package de.ultical.backend.data.mapper;

import java.util.List;

import de.ultical.backend.model.*;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

public interface DivisionConfirmationMapper extends BaseMapper<DivisionConfirmation> {

    final String divisionSelect = "SELECT dc.id, dc.version, dc.division_registration, dc.event,dc.individual_assignment, dr.is_player_registration FROM DIVISION_CONFIRMATION dc LEFT JOIN DIVISION_REGISTRATION dr ON dr.id = dc.division_registration";

    @Insert("INSERT INTO DIVISION_CONFIRMATION (division_registration, event, individual_assignment) " + "VALUES (#{divisionRegistrationId}, #{eventId}, #{isIndividualAssignment})")
    Integer insert(@Param("eventId") int eventId,
                   @Param("divisionRegistrationId") int divisionRegistrationId,
                   @Param("isIndividualAssignment") boolean isIndividualAssignment);

    // DELETE
    @Override
    @Delete("DELETE FROM DIVISION_CONFIRMATION WHERE id=#{id}")
    void delete(DivisionConfirmation entity);

    @Delete("DELETE FROM DIVISION_CONFIRMATION WHERE event=#{event.id}")
    void removeAllForEvent(@Param("event") Event event);

    // SELECT
    @Override
    @Select({ divisionSelect, "WHERE dc.id=#{id}" })
    @TypeDiscriminator(column = "is_player_registration", javaType = Boolean.class, cases = {
            @Case(value = "false", type = DivisionConfirmationTeams.class, results = {
                    @Result(column = "id", property = "teams", many = @Many(select = "de.ultical.backend.data.mapper.TeamRegistrationMapper.getRegistrationsForConfirmation")) }),
            @Case(value = "true", type = DivisionConfirmationPlayers.class) }, jdbcType = JdbcType.BOOLEAN)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "individual_assignment", property = "individualAssignment", javaType = Boolean.class, jdbcType = JdbcType.BOOLEAN),
            @Result(column = "division_registration", property = "divisionRegistration", javaType = DivisionRegistration.class, one = @One(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.get")) })
    DivisionConfirmation get(int id);

    @Select({ divisionSelect, "WHERE dc.event=#{eventId}" })
    @TypeDiscriminator(column = "is_player_registration", javaType = Boolean.class, cases = {
            @Case(value = "false", type = DivisionConfirmationTeams.class, results = {
                    @Result(column = "id", property = "teams", many = @Many(select = "de.ultical.backend.data.mapper.TeamRegistrationMapper.getRegistrationsForConfirmation")) }),
            @Case(value = "true", type = DivisionConfirmationPlayers.class) }, jdbcType = JdbcType.BOOLEAN)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "individual_assignment", property = "individualAssignment", javaType = Boolean.class, jdbcType = JdbcType.BOOLEAN),
            @Result(column = "division_registration", property = "divisionRegistration", javaType = DivisionRegistration.class, one = @One(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.get")) })
    List<DivisionConfirmation> getByEvent(int eventId);

    @Select({ divisionSelect, "WHERE dc.event=#{eventId}" })
    @TypeDiscriminator(column = "is_player_registration", javaType = Boolean.class, cases = {
            @Case(value = "false", type = DivisionConfirmationTeams.class),
            @Case(value = "true", type = DivisionConfirmationPlayers.class) }, jdbcType = JdbcType.BOOLEAN)
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "individual_assignment", property = "individualAssignment", javaType = Boolean.class, jdbcType = JdbcType.BOOLEAN),
            @Result(column = "division_registration", property = "divisionRegistration", javaType = DivisionRegistration.class, one = @One(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.getBasic")) })
    List<DivisionConfirmation> getBasicsByEvent(int eventId);
}
