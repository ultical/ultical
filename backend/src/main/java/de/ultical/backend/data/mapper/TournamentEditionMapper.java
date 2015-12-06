package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import de.ultical.backend.model.*;

public interface TournamentEditionMapper extends BaseMapper<TournamentEdition> {

	final String editionSelectBase = "SELECT sup.id, sup.version, sup.tournament_format, sup.alternative_name, sup.season, "
			+ "sup.registration_start, sup.registration_end, sup.fee_per_player, sup.fee_per_team, "
			+ "sup.fee_per_guest, sup.currency, sup.organizer_name, sup.organizer_email, "
			+ "sup.organizer_phone, sup.is_league, league.alternative_matchday_name "
			+ "FROM TOURNAMENT_EDITION sup LEFT JOIN TOURNAMENT_EDITION_LEAGUE league ON sup.id = league.id ";

	@Override
	@Select({ editionSelectBase, "WHERE sup.id = #{id}" })
	@TypeDiscriminator(column = "is_league", javaType = Boolean.class, cases = {
			@Case(value = "false", type = TournamentEditionSingle.class, results = {
					@Result(column = "id", property = "event", one = @One(select = "de.ultical.backend.data.mapper.EventMapper.getEventsForEdition", fetchType = FetchType.EAGER) , javaType = Event.class) }),
			@Case(value = "true", type = TournamentEditionLeague.class, results = {
					@Result(column = "alternative_matchday_name", property = "alternativeMatchdayName"),
					@Result(column = "id", property = "events", many = @Many(select = "de.ultical.backend.data.mapper.EventMapper.getEventsForEdition", fetchType = FetchType.EAGER) ) }) })
	@Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
			@Result(column = "tournament_format", property = "tournamentFormat", javaType = TournamentFormat.class, one = @One(select = "de.ultical.backend.data.mapper.TournamentFormatMapper.get") ),
			@Result(column = "alternative_name", property = "alternativeName"),
			@Result(column = "season", property = "season", javaType = Season.class, one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
			@Result(column = "registration_start", property = "registrationStart"),
			@Result(column = "registration_end", property = "registrationEnd"),
			@Result(column = "fee_per_player", property = "feePerPlayer"),
			@Result(column = "fee_per_team", property = "feePerTeam"),
			@Result(column = "fee_per_guest", property = "feePerGuest"),
			@Result(column = "currency", property = "currency"),
			@Result(column = "organizer_name", property = "organizerName"),
			@Result(column = "organizer_email", property = "organizerEmail"),
			@Result(column = "organizer_phone", property = "organizerPhone"),
			@Result(column = "id", property = "divisionRegistrations", many = @Many(select = "de.ultical.backend.data.mapper.DivisionRegistrationMapper.getRegistrationsForEdition") ) })
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
			@Result(column = "sup.fee_per_player", property = "feePerPlayer"),
			@Result(column = "sup.fee_per_team", property = "feePerTeam"),
			@Result(column = "sup.fee_per_guest", property = "feePerGuest"),
			@Result(column = "sup.currency", property = "currency"),
			@Result(column = "sup.organizer_name", property = "organizerName"),
			@Result(column = "sup.organizer_email", property = "organizerEmail"),
			@Result(column = "sup.organizer_phone", property = "organizerPhone") })
	List<TournamentEdition> getEditionsForFormat(int formatId);

	@Override
	@Select(editionSelectBase)
	@TypeDiscriminator(column = "is_league", javaType = Boolean.class, cases = {
			@Case(value = "false", type = TournamentEditionSingle.class, results = {
					@Result(column = "id", property = "event", one = @One(select = "de.ultical.backend.data.mapper.EventMapper.getEventsForEdition", fetchType = FetchType.EAGER) , javaType = Event.class) }),
			@Case(value = "true", type = TournamentEditionLeague.class, results = {
					@Result(column = "alternative_matchday_name", property = "alternativeMatchdayName"),
					@Result(column = "id", property = "events", many = @Many(select = "de.ultical.backend.data.mapper.EventMapper.getEventsForEdition", fetchType = FetchType.EAGER) ) }) })
	@Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
			@Result(column = "tournament_format", property = "tournamentFormat", javaType = TournamentFormat.class, one = @One(select = "de.ultical.backend.data.mapper.TournamentFormatMapper.get") ),
			@Result(column = "alternative_name", property = "alternativeName"),
			@Result(column = "season", property = "season", javaType = Season.class, one = @One(select = "de.ultical.backend.data.mapper.SeasonMapper.get") ),
			@Result(column = "registration_start", property = "registrationStart"),
			@Result(column = "registration_end", property = "registrationEnd"),
			@Result(column = "fee_per_player", property = "feePerPlayer"),
			@Result(column = "fee_per_team", property = "feePerTeam"),
			@Result(column = "fee_per_guest", property = "feePerGuest"),
			@Result(column = "currency", property = "currency"),
			@Result(column = "organizer_name", property = "organizerName"),
			@Result(column = "organizer_email", property = "organizerEmail"),
			@Result(column = "organizer_phone", property = "organizerPhone") })
	List<TournamentEdition> getAll();

	@Override
	@Insert({ "INSERT INTO TOURNAMENT_EDITION",
			"(tournament_format, alternative_name, season, registration_start, registration_end, fee_per_player,",
			"fee_per_team, fee_per_guest, currency, organizer_name, organizer_email, organizer_phone)",
			"VALUES (#{tournamentFormat.id},#{alternativeName, jdbcType=VARCHAR},#{season.id},#{registrationStart},#{registrationEnd},",
			"#{feePerPlayer},#{feePerTeam},#{feePerGuest},#{currency, jdbcType=VARCHAR},#{organizerName, jdbcType=VARCHAR},",
			"#{organizerEmail, jdbcType=VARCHAR},#{organizerPhone, jdbcType=VARCHAR})" })
	@Options(keyProperty = "id", useGeneratedKeys = true)
	Integer insert(TournamentEdition entity);

	@Override
	@Update({
			"UPDATE TOURNAMENT_EDITION SET version=version+1, tournament_format=#{tournamentFormat.id},alternative_name=#{alternativeName},",
			"season=#{season.id}, registration_start=#{registrationStart}, registration_end=#{registrationEnd},",
			"fee_per_player=#{feePerPlayer}, fee_per_team=#{feePerTeam}, fee_per_guest=#{feePerGuest},",
			"currency=#{currency},organizer_name=#{organizerName}, organizer_email=#{organizerEmail}, organizer_phone=#{organizerPhone}",
			"WHERE id=#{id} AND version=#{version}" })
	Integer update(TournamentEdition entity);

	@Override
	@Delete("DELETE FROM TOURNAMENT_EDITION WHERE id=#{id}")
	void delete(TournamentEdition entity);
}
