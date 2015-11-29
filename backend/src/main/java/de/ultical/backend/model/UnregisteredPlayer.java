package de.ultical.backend.model;

import de.ultical.backend.data.mapper.UnregisteredPlayerMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UnregisteredPlayer extends Player {
	private String email;

	@Override
	public Class<UnregisteredPlayerMapper> getMapper() {
		return UnregisteredPlayerMapper.class;
	}
	
	
}
