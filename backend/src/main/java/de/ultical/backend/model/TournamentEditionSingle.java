package de.ultical.backend.model;

import de.ultical.backend.data.mapper.TournamentEditionMapper;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude="event")
public class TournamentEditionSingle extends TournamentEdition {
	 private Event event;
	 
	 @Override
	public Class<TournamentEditionMapper> getMapper() {
		return TournamentEditionMapper.class;
	}
}
