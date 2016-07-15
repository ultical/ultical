package de.ultical.backend.schedule;

import de.ultical.backend.model.Team;
import lombok.Data;

@Data
public class SpiritSheet {

    private SpiritVersion spiritVersion;
    private Team teamGiving;
    private Team teamReceiving;

}
