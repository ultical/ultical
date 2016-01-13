package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import de.ultical.backend.model.Contact;
import de.ultical.backend.model.Event;
import de.ultical.backend.model.Location;

public interface EventMapper extends BaseMapper<Event> {

    // INSERT
    @Override
    @Insert({
            "INSERT INTO EVENT (matchday_number, tournament_edition, location, start_date, end_date, local_organizer, hash_tag) VALUES",
            "(#{matchdayNumber},#{tournamentEdition.id},#{location.id}, #{startDate},#{endDate},#{localOrganizer.id, jdbcType=INTEGER},#{hashTag}" })
    @Options(keyProperty = "id", useGeneratedKeys = true)
    Integer insert(Event event);

    // UPDATE
    @Override
    @Update({
            "UPDATE EVENT SET version=version+1, tournament_edition=#{tournamentEdition.id}, location=#{location.id},",
            "start_date=#{startDate}, end_date=#{endDate},",
            "hash_tag=#{hashTag}, local_organizer=#{localOrganizer.id, jdbcType=INTEGER}",
            "WHERE version=#{version} AND id=#{id}" })
    Integer update(Event entity);

    // DELETE
    @Override
    @Delete("DELETE FROM EVENT WHERE id=#{id}")
    void delete(Event entity);

    // SELECT
    @Override
    @Select("SELECT * FROM EVENT WHERE id = #{id}")
    @Results({ @Result(column = "matchday_number", property = "matchdayNumber"),
            @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "tournament_edition", property = "tournamentEdition", one = @One(select = "de.ultical.backend.data.mapper.TournamentEditionMapper.get") ),
            @Result(column = "location", property = "location", one = @One(select = "de.ultical.backend.data.mapper.LocationMapper.get") , javaType = Location.class),
            @Result(column = "start_date", property = "startDate"), @Result(column = "end_date", property = "endDate"),
            @Result(column = "id", property = "fees", many = @Many(select = "de.ultical.backend.data.mapper.FeeMapper.getForEvent") ),
            @Result(column = "hash_tag", property = "hashTag"),
            @Result(column = "local_organizer", property = "localOrganizer", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get") , javaType = Contact.class) })
    Event get(int id);

    @Override
    @Select("SELECT * FROM EVENT")
    @Results({ @Result(column = "matchday_number", property = "matchdayNumber"),
            @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "tournament_edition", property = "tournamentEdition", one = @One(select = "de.ultical.backend.data.mapper.TournamentEditionMapper.get") ),
            @Result(column = "location", property = "location", one = @One(select = "de.ultical.backend.data.mapper.LocationMapper.get") , javaType = Location.class),
            @Result(column = "start_date", property = "startDate"), @Result(column = "end_date", property = "endDate"),
            @Result(column = "id", property = "fees", many = @Many(select = "de.ultical.backend.data.mapper.FeeMapper.getForEvent") ),
            @Result(column = "hash_tag", property = "hashTag"),
            @Result(column = "local_organizer", property = "localOrganizer", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get") , javaType = Contact.class) })
    List<Event> getAll();

    @Select("SELECT * FROM EVENT WHERE tournament_edition=#{editionId}")
    @Results({ @Result(column = "matchday_number", property = "matchdayNumber"),
            @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "tournament_edition", property = "tournamentEdition", one = @One(select = "de.ultical.backend.data.mapper.TournamentEditionMapper.get") ),
            @Result(column = "location", property = "location", one = @One(select = "de.ultical.backend.data.mapper.LocationMapper.get") , javaType = Location.class),
            @Result(column = "start_date", property = "startDate"), @Result(column = "end_date", property = "endDate"),
            @Result(column = "id", property = "fees", many = @Many(select = "de.ultical.backend.data.mapper.FeeMapper.getForEvent") ),
            @Result(column = "hash_tag", property = "hashTag"),
            @Result(column = "local_organizer", property = "localOrganizer", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get") , javaType = Contact.class) })
    List<Event> getEventsForEdition(int editionId);

}
