package de.ultical.backend.model;

import java.time.LocalDate;

import javax.inject.Inject;

import de.ultical.backend.data.mapper.DfvPlayerMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DfvPlayer extends Player {
	private String dfvNumber;
	private LocalDate birthDate;

	@Inject
	public DfvPlayer() {
		System.out.println("new dfv player");
	}

	@Override
	public Class<DfvPlayerMapper> getMapper() {
		// TODO Auto-generated method stub
		return DfvPlayerMapper.class;
	}
}
