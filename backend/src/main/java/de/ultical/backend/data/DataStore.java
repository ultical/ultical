package de.ultical.backend.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

import javax.inject.Singleton;

import de.ultical.backend.model.AbstractTournament;
import de.ultical.backend.model.Event;
import de.ultical.backend.model.League;
import de.ultical.backend.model.Tournament;

/**
 * the cloud
 * 
 * @author bbe
 *
 */
@Singleton
public class DataStore {

	private Map<String, Tournament> tournamentPerName;
	private TreeSet<Event> orderedEvents;

	public DataStore() {
		this.fillDataStore();
	}

	protected void fillDataStore() {
		this.tournamentPerName = new HashMap<String, Tournament>();
		this.orderedEvents = new TreeSet<Event>(new EventDateComparator());
	}

	public boolean storeTournament(final Tournament t) {
		Objects.requireNonNull(t);
		if (t.getName() == null || t.getName().isEmpty()) {
			throw new IllegalArgumentException("A tournament's name must not be null or empty!");
		}
		// TODO further validations?
		if (this.tournamentPerName.containsKey(t.getName())) {
			throw new IllegalArgumentException(
					String.format("A tournament with the name %s already exists!", t.getName()));
		}
		this.tournamentPerName.put(t.getName(), t);
		return true;
	}

	public AbstractTournament getTournamentByName(final String tName) {
		Objects.requireNonNull(tName);
		AbstractTournament result = this.tournamentPerName.get(tName);
		return result;
	}
	
	public Collection<Tournament> getAllTournaments() {
		return this.tournamentPerName.values();
	}

	public boolean storeEvent(Event event) {
		Objects.requireNonNull(event);
		Objects.requireNonNull(event.getTournament(),
				"a corresponding Tournament or League is required for each Event");
		final AbstractTournament tournament = event.getTournament();
		if (tournament instanceof Tournament) {
			if (!event.equals(((Tournament) tournament).getEvent())) {
				throw new IllegalStateException("Reference between Tournament and Event do not match");
			}
		} else if (tournament instanceof League) {
			if (!((League) tournament).getLeagueEvents().contains(event)) {
				throw new IllegalStateException("Reference between League and Even do not match");
			}
		}
		Objects.requireNonNull(event.getStartDate(), "StartDate must not be null");
		Objects.requireNonNull(event.getEndDate(), "EndDate must not be null");
		this.orderedEvents.add(event);
		return true;
	}

	private Event fakeEvent(final Date fakeStartDate) {
		final Event result = new Event();
		result.setStartDate(fakeStartDate);
		return result;
	}

	public NavigableSet<Event> getEvents(final Date startInteravl, final Date endInterval) {
		final Event firstEvent = startInteravl == null ? this.orderedEvents.first()
				: this.orderedEvents.floor(fakeEvent(startInteravl));
		final Event lastEvent = endInterval == null ? this.orderedEvents.last()
				: this.orderedEvents.ceiling(fakeEvent(endInterval));
		NavigableSet<Event> result;
		if (firstEvent == null && lastEvent == null) {
			result = Collections.emptyNavigableSet();
		} else if (firstEvent == null && lastEvent != null) {
			result = this.orderedEvents.headSet(lastEvent, true);
		} else if (firstEvent != null && lastEvent == null) {
			result = this.orderedEvents.tailSet(firstEvent, true);
		} else {
			result = this.orderedEvents.subSet(lastEvent, true, lastEvent, true);
		}
		return Collections.unmodifiableNavigableSet(result);
	}
}
