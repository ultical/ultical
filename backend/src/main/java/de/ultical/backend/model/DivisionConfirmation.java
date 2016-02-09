package de.ultical.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DivisionConfirmation is used if not all Divisions/Teams that registered for
 * an edition are played at all events. E.g.: a league system where day 1 is
 * only Mixed and day 2 is women and open, ...
 *
 * If no divisionConfirmation is assigned to an event we assume that all
 * divisions of the respective edition will be played at this event
 *
 * @author bas
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class DivisionConfirmation extends Identifiable {
    private DivisionRegistration divisionRegistration;
    private boolean individualAssignment;
}
