package de.ultical.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.ultical.backend.api.transferClasses.DfvMvPlayer;
import de.ultical.backend.app.LocalDateDeserializer;
import de.ultical.backend.app.LocalDateSerializer;
import de.ultical.backend.app.LocalDateTimeDeserializer;
import de.ultical.backend.app.LocalDateTimeSerializer;
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
        this.setBirthDate(LocalDate.parse(dfvPlayer.getDobString()));
        this.setDfvNumber(dfvPlayer.getDfvNumber());
        this.setGender(Gender.robustValueOf(dfvPlayer.getGender()));
    }

    private int dfvNumber;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime eligibleUntil;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate birthDate;

    private Club club;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime lastModified;

    /**
     * <code>True</code> if the player is eligible for tournaments of the
     * DFV.<br />
     * This method simply checks whether {@link #eligibleUntil} is null,
     * following the assumption that the <code>eligibleUntil</code> value always
     * a point in the past.
     * 
     * @return <code>true</code> if the player is eligible for DFV tournaments,
     *         <code>false</code> otherwise.
     */
    @JsonIgnore
    public boolean isEligible() {
        return this.eligibleUntil == null;
    }

    @Override
    public Class<DfvPlayerMapper> getMapper() {
        return DfvPlayerMapper.class;
    }
}
