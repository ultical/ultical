package de.ultical.backend.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.ultical.backend.app.DivisionConfirmationDeserializer;
import de.ultical.backend.app.LocalDateDeserializer;
import de.ultical.backend.app.LocalDateSerializer;
import de.ultical.backend.data.mapper.EventMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Event extends Identifiable {

    // keep on -1 for single tournaments
    private int matchdayNumber = -1;

    private TournamentEdition tournamentEdition;
    private String name;

    private List<Location> locations;

    // subset of the tournaments divisions and participants
    @JsonDeserialize(using = DivisionConfirmationDeserializer.class)
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

    private List<Resource> resources;

    @Override
    public Class<EventMapper> getMapper() {
        return EventMapper.class;
    }
}
