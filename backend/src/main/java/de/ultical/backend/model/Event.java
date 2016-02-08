package de.ultical.backend.model;

import java.time.LocalDate;
import java.util.List;

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
public class Event extends Identifiable {

    // keep on -1 for single tournaments
    private int matchdayNumber = -1;

    private TournamentEdition tournamentEdition;

    private List<Location> locations;

    // subset of the tournaments divisions and participants
    @MinSize(1)
    private List<DivisionConfirmation> divisionConfirmations;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate startDate;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate endDate;

    private List<User> admins;
    private List<Fee> fees;

    private Contact localOrganizer;

    private String info;

    @Override
    public Class<EventMapper> getMapper() {
        return EventMapper.class;
    }
}
