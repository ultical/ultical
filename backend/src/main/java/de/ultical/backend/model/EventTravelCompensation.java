package de.ultical.backend.model;

import de.ultical.backend.data.mapper.EventTravelCompensationMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entity to store per {@link Roster} and per {@link Event} specific information
 * about the team's travelled distance and the resulting compensation.
 * 
 * @author bb
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventTravelCompensation extends Identifiable {

    /**
     * our link back to the event. As the travel-compensation is calculated on a
     * per division basis, it is important to link the event's division specific
     * part.
     */
    private Event event;

    /**
     * A link to the {@link DivisionRegistrationTeams} which is required in
     * order to distinguish the different divisions.
     */
    private DivisionRegistrationTeams divisionRegistration;
    /**
     * The team / roster for which the compensation is calculated
     */
    private Roster roster;
    /**
     * The computed distance
     */
    private Integer distance;
    /**
     * The resulting fee. (currency is €)
     */
    private Float fee;

    /**
     * Indicates whether the amount has already been paid.<br />
     * Depending on the sign of the {@link #fee} to be paid, this flag shows
     * whether the DFV has already received the money or has placed the
     * transfer.
     */
    private Boolean paid;

    @Override
    public Class<EventTravelCompensationMapper> getMapper() {
        return EventTravelCompensationMapper.class;
    }

}
