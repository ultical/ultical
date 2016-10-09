package de.ultical.backend.model;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.ultical.backend.app.LocalDateDeserializer;
import de.ultical.backend.app.LocalDateSerializer;
import de.ultical.backend.data.mapper.TournamentEditionMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class TournamentEdition extends Identifiable {

    @NotNull
    private TournamentFormat tournamentFormat;

    private String name;
    private Season season;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate registrationStart;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate registrationEnd;

    private Context context;

    private String hashtag;

    private List<Fee> fees;
    private Contact organizer;

    private List<DivisionRegistration> divisionRegistrations;

    private List<Event> events;
    private String alternativeMatchdayName;

    private boolean allowEventTeamRegManagement = true;

    @Override
    public Class<TournamentEditionMapper> getMapper() {
        return TournamentEditionMapper.class;
    }
}
