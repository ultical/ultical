package de.ultical.backend.model;

import de.ultical.backend.data.mapper.PlayerMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UnregisteredPlayer extends Player {
	private String email;

	@Override
	public Class<PlayerMapper> getMapper() {
		return PlayerMapper.class;
	}
	
	
}
