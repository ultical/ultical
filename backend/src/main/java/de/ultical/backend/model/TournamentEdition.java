package de.ultical.backend.model;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.ultical.backend.app.LocalDateDeserializer;
import de.ultical.backend.app.LocalDateSerializer;
import de.ultical.backend.data.mapper.TournamentEditionMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = TournamentEdition.class)
public class TournamentEdition extends Identifiable {

    private TournamentFormat tournamentFormat;

    private String alternativeName;
    private Season season;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate registrationStart;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate registrationEnd;

    private String hashtag;

    private List<Fee> fees;
    private Contact organizer;

    private List<DivisionRegistration> divisionRegistrations;

    private List<Event> events;
    private String alternativeMatchdayName;

    @Override
    public Class<TournamentEditionMapper> getMapper() {
        return TournamentEditionMapper.class;
    }
}
