package de.ultical.backend.model;

import de.ultical.backend.data.mapper.TeamMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class Team extends Identifiable{
	private String name;

	@Override
	public Class<TeamMapper> getMapper() {
		return TeamMapper.class;
	}

}
