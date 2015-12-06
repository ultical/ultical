package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;

import de.ultical.backend.model.Event;
import de.ultical.backend.model.Location;

public interface EventMapper extends BaseMapper<Event> {

	@Insert({ "INSERT INTO EVENT (matchday_number, tournament_edition, location, start_date, end_date,",
			"fee_per_team, fee_per_player, fee_per_guest, fee_per_breakfast, fee_per_lunch, fee_per_dinner,",
			"fee_per_night, local_organizer_name, local_organizer_email, local_organizer_phone) VALUES",
			"(#{matchdayNumber},#{tournamentEdition.id},#{location.id}, #{startDate},#{endDate},",
			"#{feePerTeam},#{feePerPlayer},#{feePerGuest},#{feePerBreakfast},#{feePerLunch},",
			"#{feePerDinner},#{feePerNight},#{localOrganizerName},#{localOrganizerEmail},#{localOrganizerPhone})" })
	@Options(keyProperty = "id", useGeneratedKeys = true)
	Integer insert(Event event);

	@Select("SELECT * FROM EVENT WHERE id = #{id}")
	@Results({ @Result(column = "matchday_number", property = "matchdayNumber"),
			@Result(column = "tournament_edition", property = "tournamentEdition", one = @One(select = "de.ultical.backend.data.mapper.TournamentEditionMapper.get") ),
			@Result(column = "location", property = "location", one = @One(select = "de.ultical.backend.data.mapper.LocationMapper.get") ),
			@Result(column = "start_date", property = "startDate"), @Result(column = "end_date", property = "endDate"),
			@Result(column = "fee_per_team", property = "feePerTeam"),
			@Result(column = "fee_per_player", property = "feePerPlayer"),
			@Result(column = "fee_per_guest", property = "feePerGuest"),
			@Result(column = "fee_per_breakfast", property = "feePerBreakfast"),
			@Result(column = "fee_per_lunch", property = "feePerLunch"),
			@Result(column = "fee_per_dinner", property = "feePerDinner"),
			@Result(column = "fee_per_night", property = "feePerNight"),
			@Result(column = "local_organizer_name", property = "localOrganizerName"),
			@Result(column = "local_organizer_email", property = "localOrganizerEmail"),
			@Result(column = "local_organizer_phone", property = "localOrganizerPhone") })
	Event get(int id);

	@Select("SELECT * FROM EVENT")
	@Results({ @Result(column = "matchday_number", property = "matchdayNumber"),
		@Result(column = "tournament_edition", property = "tournamentEdition", one = @One(select = "de.ultical.backend.data.mapper.TournamentEditionMapper.get") ),
		@Result(column = "location", property = "location", one = @One(select = "de.ultical.backend.data.mapper.LocationMapper.get"), javaType=Location.class ),
		@Result(column = "start_date", property = "startDate"), @Result(column = "end_date", property = "endDate"),
		@Result(column = "fee_per_team", property = "feePerTeam"),
		@Result(column = "fee_per_player", property = "feePerPlayer"),
		@Result(column = "fee_per_guest", property = "feePerGuest"),
		@Result(column = "fee_per_breakfast", property = "feePerBreakfast"),
		@Result(column = "fee_per_lunch", property = "feePerLunch"),
		@Result(column = "fee_per_dinner", property = "feePerDinner"),
		@Result(column = "fee_per_night", property = "feePerNight"),
		@Result(column = "local_organizer_name", property = "localOrganizerName"),
		@Result(column = "local_organizer_email", property = "localOrganizerEmail"),
		@Result(column = "local_organizer_phone", property = "localOrganizerPhone") })
	List<Event> getAll();
	
	@Select("SELECT * FROM EVENT WHERE tournament_edition=#{editionId}")
	@Results({ @Result(column = "matchday_number", property = "matchdayNumber"),
		@Result(column = "tournament_edition", property = "tournamentEdition", one = @One(select = "de.ultical.backend.data.mapper.TournamentEditionMapper.get") ),
		@Result(column = "location", property = "location", one = @One(select = "de.ultical.backend.data.mapper.LocationMapper.get") ),
		@Result(column = "start_date", property = "startDate"), @Result(column = "end_date", property = "endDate"),
		@Result(column = "fee_per_team", property = "feePerTeam"),
		@Result(column = "fee_per_player", property = "feePerPlayer"),
		@Result(column = "fee_per_guest", property = "feePerGuest"),
		@Result(column = "fee_per_breakfast", property = "feePerBreakfast"),
		@Result(column = "fee_per_lunch", property = "feePerLunch"),
		@Result(column = "fee_per_dinner", property = "feePerDinner"),
		@Result(column = "fee_per_night", property = "feePerNight"),
		@Result(column = "local_organizer_name", property = "localOrganizerName"),
		@Result(column = "local_organizer_email", property = "localOrganizerEmail"),
		@Result(column = "local_organizer_phone", property = "localOrganizerPhone") })
	List<Event> getEventsForEdition(int editionId);
	
	@Override
	@Update({"UPDATE EVENT SET version=version+1, tournament_edition=#{tournamentEdition.id}, location=#{location.id},",
		"start_date=#{startDate}, end_date=#{endDate}, fee_per_team=#{feePerTeam}, fee_per_player=#{feePerPlayer},",
		"fee_per_guest=#{feePerGuest}, fee_per_breakfast=#{feePerBreakfast}, fee_per_lunch=#{feePerLunch}, fee_per_dinner=#{feePerDinner},",
		"fee_per_night=#{feePerNight}, local_organizer_name=#{localOrganizerName}, local_organizer_email=#{localOrganizerEmail}, local_organizer_phone=#{localOrganizerPhone}",
		"WHERE version=#{version} AND id=#{id}"})
	Integer update(Event entity);
	
	@Override
	@Delete("DELETE FROM EVENT WHERE id=#{id}")
	void delete(Event entity);
}
