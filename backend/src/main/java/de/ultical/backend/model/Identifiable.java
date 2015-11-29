package de.ultical.backend.model;

import de.ultical.backend.data.mapper.BaseMapper;
import lombok.Data;

@Data
public abstract class Identifiable {
	private int id;
	private int version;

	public abstract Class<? extends BaseMapper<? extends Identifiable>> getMapper();
}
