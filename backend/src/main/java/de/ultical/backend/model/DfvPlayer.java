package de.ultical.backend.model;

import java.time.LocalDate;

import de.ultical.backend.api.transferClasses.DfvMvPlayer;
import de.ultical.backend.data.mapper.DfvPlayerMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DfvPlayer extends Player {

    public DfvPlayer() {
        super();
    }

    public DfvPlayer(DfvMvPlayer dfvPlayer) {
        super();
        this.setBirthDate(LocalDate.parse(dfvPlayer.getGeburtsdatum()));
        this.setDfvNumber(dfvPlayer.getDfvnr());
        this.setGender(dfvPlayer.getGeschlecht().equalsIgnoreCase("m") ? Gender.MALE
                : dfvPlayer.getGeschlecht().equalsIgnoreCase("w") ? Gender.FEMALE : Gender.NA);
    }

    private int dfvNumber;
    private LocalDate birthDate;
    private Club club;

    @Override
    public Class<DfvPlayerMapper> getMapper() {
        // TODO Auto-generated method stub
        return DfvPlayerMapper.class;
    }
}
