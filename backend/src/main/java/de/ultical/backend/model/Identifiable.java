package de.ultical.backend.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.ultical.backend.data.mapper.BaseMapper;
import lombok.Data;

@Data
@JsonIdentityInfo(	generator = ObjectIdGenerators.PropertyGenerator.class, property="id")
public abstract class Identifiable {
	
	private int id;
	private int version;

	public abstract Class<? extends BaseMapper<? extends Identifiable>> getMapper();
}
