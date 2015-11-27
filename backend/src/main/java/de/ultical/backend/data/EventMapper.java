package de.ultical.backend.data;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import de.ultical.backend.model.Event;

public interface EventMapper {

	@Insert("INSERT INTO event")
	public void insertEvent(Event event);

	@Select("SELECT * FROM event WHERE id = #{id}")
	public Event getEvent(int id);
	
	@Select("SELECT * FROM event")
	public List<Event> getEvents();
}
