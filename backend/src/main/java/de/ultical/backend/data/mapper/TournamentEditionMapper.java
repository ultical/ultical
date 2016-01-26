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
import org.apache.ibatis.mapping.FetchType;

import de.ultical.backend.model.Season;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.TournamentFormat;

public interface TournamentEditionMapper extends BaseMapper<TournamentEdition> {

    final String editionSelectBase = "SELECT id, version, tournament_format, alternative_name, season, "
            + "registration_start, registration_end, hashtag, organizer, alternative_matchday_name "
            + "FROM TOURNAMENT_EDITION";

    // INSERT
    @Override
    @Insert({ "INSERT INTO TOURNAMENT_EDITION",
            "(tournament_format, alternative_name, season, registration_start, registration_end, organizer, hashtag, alternative_matchday_name)",
            "VALUES (#{tournamentFormat.id, jdbcType=INTEGER},#{alternativeName, jdbcType=VARCHAR},#{season.id, jdbcType=INTEGER},",
            "#{registrationStart, jdbcType=DATE},#{registrationEnd, jdbcType=DATE},",
            "#{organizer.id, jdbcType=INTEGER}, #{hashtag, jdbcType=VARCHAR}, #{alternativeMatchdayName, jdbcType=VARCHAR})" })
    @Options(keyProperty = "id", useGeneratedKeys = true)
    Integer insert(TournamentEdition entity);

    // UPDATE
    @Override
    @Update({ "UPDATE TOURNAMENT_EDITION SET version=version+1, hashtag=#{hashtag, jdbcType=VARCHAR},",
            "tournament_format=#{tournamentFormat.id, jdbcType=INTEGER}, alternative_name=#{alternativeName, jdbcType=VARCHAR},",
            "season=#{season.id, jdbcType=INTEGER}, registration_start=#{registrationStart, jdbcType=DATE},",
            "registration_end=#{registrationEnd, jdbcType=DATE}, organizer=#{organizer.id, jdbcType=INTEGER},",
            "alternative_matchday_name=#{alternativeMatchdayName, jdbcType=VARCHAR}",
            "WHERE id=#{id} AND version=#{version}" })
    Integer update(TournamentEdition entity);

    // DELETE
    @Override
    @Delete("DELETE FROM TOURNAMENT_EDITION WHERE id=#{id}")
    void delete(TournamentEdition entity);

    // SELECT
    @Override
    @Select({ editionSelectBase, "WHERE id = #{id}" })
    @Results({ @Result(column = "alternative_matchday_name", property = "alternativeMatchdayName"),
            @Result(column = "id", property = "events", many = @Many(select = "de.ultical.backend.data.mapper.EventMapper.getEventsForEdition", fetchType = FetchType.EAGER) ),
            @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "tournament_format", property = "tournamentFormat", javaType = TournamentFormat.class, one = @One(select = "de.ultical.backend.data.mapper.TournamentFormatMapper.get") ),
            @Result(column = "alternative_name", property = "alternativeName"),
            @Result(column = "season", property = "season", javaType = Season.class, one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
            @Result(column = "registration_start", property = "registrationStart"),
            @Result(column = "registration_end", property = "registrationEnd"),
            @Result(column = "hashtag", property = "hashtag"),
            @Result(column = "id", property = "fees", many = @Many(select = "de.ultical.backend.data.mapper.FeeMapper.getForTournamentEdition") ),
            @Result(column = "organizer", property = "organizer", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get") ),
            @Result(column = "id", property = "divisionRegistrations", many = @Many(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.getRegistrationsForEdition") ) })
    TournamentEdition get(int id);

    // use this method to get Editions when loading an event - so no event
    // entities should be included (double happiness)
    @Select({ editionSelectBase, "WHERE id = #{id}" })
    @Results({ @Result(column = "alternative_matchday_name", property = "alternativeMatchdayName"),
            @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "tournament_format", property = "tournamentFormat", javaType = TournamentFormat.class, one = @One(select = "de.ultical.backend.data.mapper.TournamentFormatMapper.getForEdition") ),
            @Result(column = "alternative_name", property = "alternativeName"),
            @Result(column = "season", property = "season", javaType = Season.class, one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
            @Result(column = "registration_start", property = "registrationStart"),
            @Result(column = "registration_end", property = "registrationEnd"),
            @Result(column = "hashtag", property = "hashtag"),
            @Result(column = "id", property = "fees", many = @Many(select = "de.ultical.backend.data.mapper.FeeMapper.getForTournamentEdition") ),
            @Result(column = "organizer", property = "organizer", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get") ),
            @Result(column = "id", property = "divisionRegistrations", many = @Many(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.getRegistrationsForEdition") ) })
    TournamentEdition getForEvent(int id);

    @Select({ editionSelectBase, "WHERE tournament_format = #{formatId}" })
    @Results({ @Result(column = "alternative_matchday_name", property = "alternativeMatchdayName"),
            @Result(column = "id", property = "events", many = @Many(select = "de.ultical.backend.data.mapper.EventMapper.getEventsForEdition", fetchType = FetchType.EAGER) ),
            @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "alternative_name", property = "alternativeName"),
            @Result(column = "season", property = "season", javaType = Season.class, one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
            @Result(column = "registration_start", property = "registrationStart"),
            @Result(column = "registration_end", property = "registrationEnd"),
            @Result(column = "hashtag", property = "hashtag"),
            @Result(column = "id", property = "fees", many = @Many(select = "de.ultical.backend.data.mapper.FeeMapper.getForTournamentEdition") ),
            @Result(column = "organizer", property = "organizer", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get") ),
            @Result(column = "id", property = "divisionRegistrations", many = @Many(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.getRegistrationsForEdition") ) })
    List<TournamentEdition> getEditionsForFormat(int formatId);

    @Override
    @Select(editionSelectBase)
    @Results({ @Result(column = "alternative_matchday_name", property = "alternativeMatchdayName"),
            @Result(column = "id", property = "events", many = @Many(select = "de.ultical.backend.data.mapper.EventMapper.getEventsForEdition", fetchType = FetchType.EAGER) ),
            @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "tournament_format", property = "tournamentFormat", javaType = TournamentFormat.class, one = @One(select = "de.ultical.backend.data.mapper.TournamentFormatMapper.getForEdition") ),
            @Result(column = "alternative_name", property = "alternativeName"),
            @Result(column = "season", property = "season", javaType = Season.class, one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
            @Result(column = "registration_start", property = "registrationStart"),
            @Result(column = "registration_end", property = "registrationEnd"),
            @Result(column = "hashtag", property = "hashtag"),
            @Result(column = "id", property = "fees", many = @Many(select = "de.ultical.backend.data.mapper.FeeMapper.getForTournamentEdition") ),
            @Result(column = "organizer", property = "organizer", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get") ),
            @Result(column = "id", property = "divisionRegistrations", many = @Many(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.getRegistrationsForEdition") ) })
    List<TournamentEdition> getAll();

}
