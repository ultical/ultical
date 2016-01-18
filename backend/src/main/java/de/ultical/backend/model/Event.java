package de.ultical.backend.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.ultical.backend.app.LocalDateDeserializer;
import de.ultical.backend.app.LocalDateSerializer;
import de.ultical.backend.data.mapper.EventMapper;
import io.dropwizard.validation.MinSize;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Event.class)
public class Event extends Identifiable {

    // keep on -1 for single tournaments
    private int matchdayNumber = -1;

    private TournamentEdition tournamentEdition;

    private Location location;

    // subset of the tournaments divisions and participants
    @MinSize(1)
    private Set<DivisionConfirmation> divisionConfirmations;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate startDate;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate endDate;

    private List<User> admins;
    private List<Fee> fees;

    private Contact localOrganizer;

    @Override
    public Class<EventMapper> getMapper() {
        return EventMapper.class;
    }
}
