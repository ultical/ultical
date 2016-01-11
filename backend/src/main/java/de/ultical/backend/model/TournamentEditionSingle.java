package de.ultical.backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import de.ultical.backend.data.mapper.TournamentEditionMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = "event")
public class TournamentEditionSingle extends TournamentEdition {

	@JsonManagedReference
	private Event event;

	@Override
	public Class<TournamentEditionMapper> getMapper() {
		return TournamentEditionMapper.class;
	}
}
