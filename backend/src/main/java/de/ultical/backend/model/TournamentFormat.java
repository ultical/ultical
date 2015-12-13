package de.ultical.backend.model;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import de.ultical.backend.data.mapper.TournamentFormatMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TournamentFormat extends Identifiable {


	private String name;

	private String description;

	@JsonManagedReference
	private List<TournamentEdition> editions;

	private Set<User> admins;

	@Override
	public Class<TournamentFormatMapper> getMapper() {
		return TournamentFormatMapper.class;
	}
}
