package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import de.ultical.backend.model.EventTravelCompensation;

public interface EventTravelCompensationMapper extends BaseMapper<EventTravelCompensation> {

    @Select("SELECT * FROM EVENT_TRAVEL_COMPENSATION WHERE id=#{id}")
    @Results({
            @Result(column = "roster", property = "roster", one = @One(select = "de.ultical.backend.data.mapper.RosterMapper.get")),
            @Result(column = "event", property = "event", one = @One(select = "de.ultical.backend.data.mapper.EventMapper.get")),
            @Result(column = "division_registration", property = "divisionRegistration", one = @One(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.get")) })
    @Override
    EventTravelCompensation get(int id);

    @Override
    @Select("SELECT * FROM EVENT_TRAVEL_COMPENSATION")
    @Results({
            @Result(column = "roster", property = "roster", one = @One(select = "de.ultical.backend.data.mapper.RosterMapper.get")),
            @Result(column = "event", property = "event", one = @One(select = "de.ultical.backend.data.mapper.EventMapper.get")),
            @Result(column = "division_registration", property = "divisionRegistration", one = @One(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.get")) })
    List<EventTravelCompensation> getAll();

    // INSERT
    @Override
    @Insert("INSERT INTO EVENT_TRAVEL_COMPENSATION (event, division_registration, roster, distance, fee, paid) VALUES (#{event.id},#{divisionRegistration.id},#{roster.id},#{distance},#{fee},#{paid})")
    @Options(keyProperty = "id", useGeneratedKeys = true)
    Integer insert(EventTravelCompensation etc);

    // DELETE
    @Override
    @Delete("DELETE FROM EVENT_TRAVEL_COMPENSATION where id = #{id}")
    void delete(EventTravelCompensation entity);

    // UPDATE
    @Override
    @Update({ "UPDATE EVENT_TRAVEL_COMPENSATION SET", "event=#{event.id},",
            "division_registration=#{divisionRegistration.id},", "roster=#{roster.id},",
            "distance=#{distance,jdbcType=INTEGER},", "fee=#{fee,jdbcType=FLOAT},", "paid=#{paid,jdbcType=BOOLEAN},",
            "version=version+1", "WHERE id=#{id} and version=#{version}" })
    Integer update(EventTravelCompensation entity);
}
