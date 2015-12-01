package de.ultical.backend.data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;

import de.ultical.backend.data.mapper.EventMapper;
import de.ultical.backend.model.DivisionAge;
import de.ultical.backend.model.DivisionRegistration;
import de.ultical.backend.model.DivisionRegistrationTeams;
import de.ultical.backend.model.DivisionType;
import de.ultical.backend.model.Event;
import de.ultical.backend.model.Location;
import de.ultical.backend.model.Season;
import de.ultical.backend.model.Surface;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.TournamentEditionLeague;
import de.ultical.backend.model.TournamentEditionSingle;
import de.ultical.backend.model.TournamentFormat;
import de.ultical.backend.model.User;

/**
 * the cloud
 *
 * @author bbe
 *
 */
public class DataStore {

	@Inject
	SqlSession sqlSession;

	private Map<String, TournamentEdition> tournamentPerName;
	private TreeSet<Event> orderedEvents;
	private Set<Event> events;

	public DataStore() {
		this.fillDataStore();
	}

	protected void fillDataStore() {
		this.tournamentPerName = new HashMap<String, TournamentEdition>();
		this.orderedEvents = new TreeSet<Event>();
		this.events = new HashSet<Event>();

		this.fillForTesting();
	}

	public boolean storeTournament(final TournamentEdition tournamentEdition) {
		Objects.requireNonNull(tournamentEdition);
		if (tournamentEdition.getTournamentFormat().getName() == null || tournamentEdition.getTournamentFormat().getName().isEmpty()) {
			throw new IllegalArgumentException("A tournament's name must not be null or empty!");
		}
		// TODO further validations?
		if (this.tournamentPerName.containsKey(tournamentEdition.getTournamentFormat().getName())) {
			throw new IllegalArgumentException(String.format("A tournament with the name %s already exists!", tournamentEdition.getTournamentFormat().getName()));
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
		Objects.requireNonNull(event.getTournamentEdition(), "a corresponding Tournament or League is required for each Event");
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
		EventMapper eventMapper = this.sqlSession.getMapper(EventMapper.class);
		final Event event = eventMapper.get(eventId);
		return event;
	}

	public List<Event> getAllEvents() {
		List<Event> list = new ArrayList<Event>();
		list.addAll(this.events);
		return list;
		// EventMapper eventMapper =
		// this.sqlSession.getMapper(EventMapper.class);
		// final List<Event> result = eventMapper.getAll();
		// return result;
	}

	public NavigableSet<Event> getEvents(final LocalDate startInterval, final LocalDate endInterval) {
		final Event firstEvent = startInterval != null ? this.orderedEvents.first() : this.orderedEvents.floor(this.fakeEvent(startInterval));
		final Event lastEvent = endInterval != null ? this.orderedEvents.last() : this.orderedEvents.ceiling(this.fakeEvent(endInterval));
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

	private void fillForTesting() {

		/* USERS */
		User bas = new User();
		bas.setId(1);
		bas.setVersion(1);
		bas.setUsername("bas");
		bas.setEmail("bas@knallbude.de");
		bas.setPassword("password");

		Set<User> admins = new HashSet<User>();
		admins.add(bas);

		User basil = new User();
		basil.setId(2);
		basil.setVersion(1);
		basil.setUsername("basil");
		basil.setEmail("kaffee@trinkr.com");
		basil.setPassword("password2");

		Set<User> altAdmins = new HashSet<User>();
		altAdmins.add(basil);

		/* SEASONS */
		Season season16 = new Season();
		season16.setSurface(Surface.TURF);
		season16.setYear(2016);
		season16.setPlusOneYear(false);

		Season season1516 = new Season();
		season1516.setSurface(Surface.GYM);
		season1516.setYear(2015);
		season1516.setPlusOneYear(true);

		/* LOCATIONS */
		Location loc1 = new Location();
		loc1.setCity("Berlin");
		loc1.setCountry("DE");
		loc1.setStreet("Weserstr. 37");
		loc1.setZipcode(12045);
		loc1.setAdditionalInfo("Nicht abbiegen");

		Location loc2 = new Location();
		loc2.setCity("Marburg");
		loc2.setCountry("DE");
		loc2.setStreet("Afföllerwiesen 2");
		loc2.setZipcode(12345);

		/* 4 FERKEL */
		TournamentFormat tf = new TournamentFormat();
		tf.setId(1);
		tf.setName("4 Ferkel");
		tf.setAdmins(admins);
		tf.setDescription(
				"Das 4 Ferkel ist ein Turnier der Superlative. Nicht nur alteingesessene Hasen kommen ins Schwärmen, wenn sie auf die Afföllerwiesen im schönen Marburg auflaufen.\n\n Ein Genuss für die ganze Familie");
		TournamentEditionSingle te = new TournamentEditionSingle();
		te.setSeason(season16);
		te.setTournamentFormat(tf);
		te.setRegistrationStart(LocalDate.parse("2016-03-01"));
		te.setRegistrationStop(LocalDate.of(2016, 4, 30));

		DivisionRegistration drt = new DivisionRegistrationTeams();
		drt.setDivisionAge(DivisionAge.REGULAR);
		drt.setDivisionType(DivisionType.OPEN);

		Set<DivisionRegistration> divs = new HashSet<DivisionRegistration>();
		divs.add(drt);

		drt = new DivisionRegistrationTeams();
		drt.setDivisionAge(DivisionAge.U17);
		drt.setDivisionType(DivisionType.WOMEN);

		divs.add(drt);

		te.setDivisionRegistrations(divs);

		Event e = new Event();
		e.setId(1);
		e.setStartDate(LocalDate.of(2016, 6, 13));
		e.setEndDate(LocalDate.of(2016, 6, 14));
		e.setLocation(loc2);
		e.setTournamentEdition(te);

		// te.setEvent(e);

		this.tournamentPerName.put(tf.getName(), te);
		this.events.add(e);

		/* A-RELI */
		tf = new TournamentFormat();
		tf.setId(2);
		tf.setName("A-Reli");
		tf.setAdmins(admins);
		tf.setDescription("Ein Turnier der Ultimate Abteilung des DFV");

		TournamentEditionLeague tel = new TournamentEditionLeague();
		tel.setAlternativeName("32. A-Relegation");
		tel.setAlternativeMatchdayName("Spültag");
		tel.setSeason(season16);
		tel.setTournamentFormat(tf);
		tel.setRegistrationStart(LocalDate.of(2016, 1, 1));
		tel.setRegistrationStop(LocalDate.of(2016, 3, 30));

		drt = new DivisionRegistrationTeams();
		drt.setDivisionAge(DivisionAge.REGULAR);
		drt.setDivisionType(DivisionType.OPEN);

		divs = new HashSet<DivisionRegistration>();
		divs.add(drt);

		tel.setDivisionRegistrations(divs);

		Event e1 = new Event();
		e1.setId(2);
		e1.setMatchdayNumber(1);
		e1.setStartDate(LocalDate.of(2016, 5, 13));
		e1.setEndDate(LocalDate.of(2016, 5, 14));
		e1.setLocation(loc1);
		e1.setTournamentEdition(tel);

		Event e2 = new Event();
		e2.setId(3);
		e2.setMatchdayNumber(2);
		e2.setStartDate(LocalDate.of(2016, 7, 3));
		e2.setEndDate(LocalDate.of(2016, 7, 4));
		// e2.setLocation(loc2);
		e2.setTournamentEdition(tel);
		e2.setAdmins(altAdmins);

		Set<Event> aReliEvents = new HashSet<Event>();
		aReliEvents.add(e1);
		aReliEvents.add(e2);
		// tel.setEvents(aReliEvents);

		this.tournamentPerName.put(tf.getName(), te);
		this.events.add(e1);
		this.events.add(e2);

		/* WINTERLIGA */

		tf = new TournamentFormat();
		tf.setId(3);
		tf.setName("Winterliga Berlin/Brandenburg");
		tf.setAdmins(admins);
		tf.setDescription("Ein Indoor Spaß für groß und klein!");

		tel = new TournamentEditionLeague();
		tel.setSeason(season1516);
		tel.setTournamentFormat(tf);
		// tel.setRegistrationStart(new LocalDate(2015, 11, 12));
		// tel.setRegistrationStop(new LocalDate(2015, 12, 24));

		drt = new DivisionRegistrationTeams();
		drt.setDivisionAge(DivisionAge.REGULAR);
		drt.setDivisionType(DivisionType.OPEN);

		drt = new DivisionRegistrationTeams();
		drt.setDivisionAge(DivisionAge.REGULAR);
		drt.setDivisionType(DivisionType.MIXED);

		divs = new HashSet<DivisionRegistration>();
		divs.add(drt);

		tel.setDivisionRegistrations(divs);

		e1 = new Event();
		e1.setId(4);
		e1.setMatchdayNumber(1);
		e1.setStartDate(LocalDate.of(2016, 1, 13));
		e1.setEndDate(LocalDate.of(2016, 1, 14));
		e1.setLocation(loc1);
		e1.setTournamentEdition(tel);

		e2 = new Event();
		e2.setId(5);
		e2.setMatchdayNumber(2);
		e2.setStartDate(LocalDate.of(2016, 2, 3));
		e2.setEndDate(LocalDate.of(2016, 2, 4));
		e2.setLocation(loc2);
		e2.setTournamentEdition(tel);
		e2.setAdmins(altAdmins);

		Set<Event> winterligaEvents = new HashSet<Event>();
		winterligaEvents.add(e1);
		winterligaEvents.add(e2);
		// tel.setEvents(winterligaEvents);

		this.tournamentPerName.put(tf.getName(), te);
		this.events.add(e1);
		this.events.add(e2);

	}
}
