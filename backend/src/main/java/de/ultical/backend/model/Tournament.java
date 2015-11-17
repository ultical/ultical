package de.ultical.backend.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true, doNotUseGetters=true)
public class Tournament extends AbstractTournament {

	private Date registrationOpens;
	private Date registrationCloses;
	private Event event;
}
