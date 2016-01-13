package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Case;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.TypeDiscriminator;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.mapping.FetchType;

import de.ultical.backend.model.Event;
import de.ultical.backend.model.Season;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.TournamentEditionLeague;
import de.ultical.backend.model.TournamentEditionSingle;
import de.ultical.backend.model.TournamentFormat;

public interface TournamentEditionMapper extends BaseMapper<TournamentEdition> {

    final String editionSelectBase = "SELECT sup.id, sup.version, sup.tournament_format, sup.alternative_name, sup.season, "
            + "sup.registration_start, sup.registration_end,"
            + "sup.organizer, sup.is_league, league.alternative_matchday_name "
            + "FROM TOURNAMENT_EDITION sup LEFT JOIN TOURNAMENT_EDITION_LEAGUE league ON sup.id = league.id ";

    // INSERT
    @Override
    @Insert({ "INSERT INTO TOURNAMENT_EDITION",
            "(tournament_format, alternative_name, season, registration_start, registration_end, organizer)",
            "VALUES (#{tournamentFormat.id},#{alternativeName, jdbcType=VARCHAR},#{season.id},#{registrationStart},#{registrationEnd},",
            "#{organizer.id})" })
    @Options(keyProperty = "id", useGeneratedKeys = true)
    Integer insert(TournamentEdition entity);

    // UPDATE
    @Override
    @Update({
            "UPDATE TOURNAMENT_EDITION SET version=version+1, tournament_format=#{tournamentFormat.id},alternative_name=#{alternativeName},",
            "season=#{season.id}, registration_start=#{registrationStart}, registration_end=#{registrationEnd}, organizer=#{organizer.id}",
            "WHERE id=#{id} AND version=#{version}" })
    Integer update(TournamentEdition entity);

    // DELETE
    @Override
    @Delete("DELETE FROM TOURNAMENT_EDITION WHERE id=#{id}")
    void delete(TournamentEdition entity);

    // SELECT
    @Override
    @Select({ editionSelectBase, "WHERE sup.id = #{id}" })
    @TypeDiscriminator(column = "is_league", javaType = Boolean.class, cases = {
            @Case(value = "false", type = TournamentEditionSingle.class, results = {
                    @Result(column = "id", property = "event", one = @One(select = "de.ultical.backend.data.mapper.EventMapper.getEventsForEdition", fetchType = FetchType.EAGER) , javaType = Event.class) }),
            @Case(value = "true", type = TournamentEditionLeague.class, results = {
                    @Result(column = "alternative_matchday_name", property = "alternativeMatchdayName"),
                    @Result(column = "id", property = "events", many = @Many(select = "de.ultical.backend.data.mapper.EventMapper.getEventsForEdition", fetchType = FetchType.EAGER) ) }) })
    @Results({ @Result(column = "sup.id", property = "id"), @Result(column = "sup.version", property = "version"),
            @Result(column = "sup.tournament_format", property = "tournamentFormat", javaType = TournamentFormat.class, one = @One(select = "de.ultical.backend.data.mapper.TournamentFormatMapper.get") ),
            @Result(column = "sup.alternative_name", property = "alternativeName"),
            @Result(column = "sup.season", property = "season", javaType = Season.class, one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
            @Result(column = "sup.registration_start", property = "registrationStart"),
            @Result(column = "sup.registration_end", property = "registrationEnd"),
            @Result(column = "sup.id", property = "fees", many = @Many(select = "de.ultical.backend.data.mapper.FeeMapper.getForTournamentEdition") ),
            @Result(column = "sup.organizer", property = "organizer", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get") ),
            @Result(column = "sup.id", property = "divisionRegistrations", many = @Many(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.getRegistrationsForEdition") ) })
    TournamentEdition get(int id);

    @Select({ editionSelectBase, "WHERE sup.tournament_format = #{formatId}" })
    @TypeDiscriminator(column = "is_league", javaType = Boolean.class, cases = {
            @Case(value = "false", type = TournamentEditionSingle.class, results = {
                    @Result(column = "sup.id", property = "event", one = @One(select = "de.ultical.backend.data.mapper.EventMapper.getEventsForEdition", fetchType = FetchType.EAGER) , javaType = Event.class) }),
            @Case(value = "true", type = TournamentEditionLeague.class, results = {
                    @Result(column = "league.alternative_matchday_name", property = "alternativeMatchdayName"),
                    @Result(column = "sup.id", property = "events", many = @Many(select = "de.ultical.backend.data.mapper.EventMapper.getEventsForEdition", fetchType = FetchType.EAGER) ) }) })
    @Results({ @Result(column = "sup.id", property = "id"), @Result(column = "sup.version", property = "version"),
            @Result(column = "sup.tournament_format", property = "tournamentFormat", javaType = TournamentFormat.class, one = @One(select = "de.ultical.backend.data.mapper.TournamentFormatMapper.get") ),
            @Result(column = "sup.alternative_name", property = "alternativeName"),
            @Result(column = "sup.season", property = "season", javaType = Season.class, one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
            @Result(column = "sup.registration_start", property = "registrationStart"),
            @Result(column = "sup.registration_end", property = "registrationEnd"),
            @Result(column = "sup.id", property = "fees", many = @Many(select = "de.ultical.backend.data.mapper.FeeMapper.getForTournamentEdition") ),
            @Result(column = "sup.organizer", property = "organizer", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get") ),
            @Result(column = "sup.id", property = "divisionRegistrations", many = @Many(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.getRegistrationsForEdition") ) })
    List<TournamentEdition> getEditionsForFormat(int formatId);

    @Override
    @Select(editionSelectBase)
    @TypeDiscriminator(column = "is_league", javaType = Boolean.class, cases = {
            @Case(value = "false", type = TournamentEditionSingle.class, results = {
                    @Result(column = "id", property = "event", one = @One(select = "de.ultical.backend.data.mapper.EventMapper.getEventsForEdition", fetchType = FetchType.EAGER) , javaType = Event.class) }),
            @Case(value = "true", type = TournamentEditionLeague.class, results = {
                    @Result(column = "alternative_matchday_name", property = "alternativeMatchdayName"),
                    @Result(column = "id", property = "events", many = @Many(select = "de.ultical.backend.data.mapper.EventMapper.getEventsForEdition", fetchType = FetchType.EAGER) ) }) })
    @Results({ @Result(column = "sup.id", property = "id"), @Result(column = "sup.version", property = "version"),
            @Result(column = "sup.tournament_format", property = "tournamentFormat", javaType = TournamentFormat.class, one = @One(select = "de.ultical.backend.data.mapper.TournamentFormatMapper.get") ),
            @Result(column = "sup.alternative_name", property = "alternativeName"),
            @Result(column = "sup.season", property = "season", javaType = Season.class, one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
            @Result(column = "sup.registration_start", property = "registrationStart"),
            @Result(column = "sup.registration_end", property = "registrationEnd"),
            @Result(column = "sup.id", property = "fees", many = @Many(select = "de.ultical.backend.data.mapper.FeeMapper.getForTournamentEdition") ),
            @Result(column = "sup.organizer", property = "organizer", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get") ),
            @Result(column = "sup.id", property = "divisionRegistrations", many = @Many(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.getRegistrationsForEdition") ) })
    List<TournamentEdition> getAll();

}
