package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;

import de.ultical.backend.model.Event;

public interface EventMapper extends BaseMapper<Event>{

	@Insert("INSERT INTO event")
	public Integer insert(Event event);

	@Select("SELECT * FROM event WHERE id = #{id}")
	public Event get(int id);
	
	@Select("SELECT * FROM event")
	public List<Event> getAll();
}
