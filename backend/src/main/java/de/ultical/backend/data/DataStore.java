package de.ultical.backend.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

import javax.inject.Singleton;

import org.apache.ibatis.session.SqlSession;
import org.joda.time.LocalDate;

import de.ultical.backend.model.Event;
import de.ultical.backend.model.TournamentEdition;

/**
 * the cloud
 *
 * @author bbe
 *
 */
@Singleton
public class DataStore {

	private Map<String, TournamentEdition> tournamentPerName;
	private TreeSet<Event> orderedEvents;

	public DataStore() {
		this.fillDataStore();
	}

	protected void fillDataStore() {
		this.tournamentPerName = new HashMap<String, TournamentEdition>();
		this.orderedEvents = new TreeSet<Event>(new EventDateComparator());
	}

	public boolean storeTournament(final TournamentEdition tournamentEdition) {
		Objects.requireNonNull(tournamentEdition);
		if (tournamentEdition.getTournamentFormat().getName() == null
				|| tournamentEdition.getTournamentFormat().getName().isEmpty()) {
			throw new IllegalArgumentException("A tournament's name must not be null or empty!");
		}
		// TODO further validations?
		if (this.tournamentPerName.containsKey(tournamentEdition.getTournamentFormat().getName())) {
			throw new IllegalArgumentException(String.format("A tournament with the name %s already exists!",
					tournamentEdition.getTournamentFormat().getName()));
		}
		this.tournamentPerName.put(tournamentEdition.getTournamentFormat().getName(), tournamentEdition);
		return true;
	}

	public TournamentEdition getTournamentByName(final String tournamentName) {
		Objects.requireNonNull(tournamentName);

		TournamentEdition result = this.tournamentPerName.get(tournamentName);
		return result;
	}

	public Collection<TournamentEdition> getAllTournaments() {
		return this.tournamentPerName.values();
	}

	public boolean storeEvent(Event event) {
		Objects.requireNonNull(event);
		Objects.requireNonNull(event.getTournamentEdition(),
				"a corresponding Tournament or League is required for each Event");
		// final TournamentFormat tournament =
		// event.getTournamentEdition().getTournamentFormat();
		// if (tournament instanceof TournamentFormat) {
		// if (!event.equals(((TournamentFormat) tournament).getEvent())) {
		// throw new IllegalStateException("Reference between Tournament and
		// Event do not match");
		// }
		// } else if (tournament instanceof League) {
		// if (!((League) tournament).getLeagueEvents().contains(event)) {
		// throw new IllegalStateException("Reference between League and Even do
		// not match");
		// }
		// }
		Objects.requireNonNull(event.getStartDate(), "StartDate must not be null");
		Objects.requireNonNull(event.getEndDate(), "EndDate must not be null");
		this.orderedEvents.add(event);
		return true;
	}

	private Event fakeEvent(final LocalDate fakeStartDate) {
		final Event result = new Event();
		result.setStartDate(fakeStartDate);
		return result;
	}

	public Event getEvent(int eventId) {
		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession();
		EventMapper eventMapper = sqlSession.getMapper(EventMapper.class);
		Event event = eventMapper.getEvent(eventId);
		return event;
	}

	public NavigableSet<Event> getEvents(final LocalDate startInterval, final LocalDate endInterval) {
		final Event firstEvent = startInterval != null ? this.orderedEvents.first()
				: this.orderedEvents.floor(this.fakeEvent(startInterval));
		final Event lastEvent = endInterval != null ? this.orderedEvents.last()
				: this.orderedEvents.ceiling(this.fakeEvent(endInterval));
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
