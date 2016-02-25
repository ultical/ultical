package de.ultical.backend.model;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.ultical.backend.api.transferClasses.DfvMvPlayer;
import de.ultical.backend.app.LocalDateDeserializer;
import de.ultical.backend.app.LocalDateSerializer;
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

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate birthDate;

    private Club club;

    @Override
    public Class<DfvPlayerMapper> getMapper() {
        return DfvPlayerMapper.class;
    }
}
