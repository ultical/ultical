package de.ultical.backend.data;

import java.time.LocalDate;
import java.util.*;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.glassfish.jersey.process.internal.RequestScoped;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.data.mapper.*;
import de.ultical.backend.model.*;

/**
 * the cloud
 *
 * @author bbe
 *
 */
@RequestScoped
public class DataStore {

	@Inject
	SqlSession sqlSession;

	@Inject
	Client client;

	private Map<String, TournamentEdition> tournamentPerName;
	private TreeSet<Event> orderedEvents;
	private Set<Event> events;
	private Set<User> users;
	private Set<DfvPlayer> dfvPlayers;

	private List<DfvMvName> dfvNames;

	public DataStore() {
		this.fillDataStore();
	}

	protected void fillDataStore() {
		this.tournamentPerName = new HashMap<String, TournamentEdition>();
		this.orderedEvents = new TreeSet<Event>();
		this.events = new HashSet<Event>();
		this.users = new HashSet<User>();
		this.dfvPlayers = new HashSet<DfvPlayer>();

		this.fillForTesting();
	}

	public <T extends Identifiable> List<T> getAll(Class<T> clazz) {
		try {
			T instance = clazz.newInstance();
			BaseMapper<T> mapper = (BaseMapper<T>) this.sqlSession.getMapper(instance.getMapper());

			return mapper.getAll();
		} catch (IllegalAccessException | InstantiationException iae) {
			throw new PersistenceException(iae);
		} finally {
			this.sqlSession.close();
		}
	}
	
	public <T extends Identifiable> T addNew(T newInstance) {
		try {
			BaseMapper<T> mapper = (BaseMapper<T>) this.sqlSession.getMapper(newInstance.getMapper());
			mapper.insert(newInstance);
			this.sqlSession.commit();
			return newInstance;
		} catch (PersistenceException pe) {
			this.sqlSession.rollback();
			throw pe;
		} finally {
			this.sqlSession.close();
		}
	}
	
	public <T extends Identifiable> boolean update(T updatedInstance) {
		try {
			BaseMapper<T> mapper = (BaseMapper<T>) this.sqlSession.getMapper(updatedInstance.getMapper());
			Integer updateCount = mapper.update(updatedInstance);
			this.sqlSession.commit();
			return updateCount == 1;
		} catch (PersistenceException pe) {
			this.sqlSession.rollback();
			throw pe;
		} finally {
			this.sqlSession.close();
		}
	}
	
	public <T extends Identifiable> T get(Integer id, Class<T> clazz) {
		try {
			T instance = clazz.newInstance();
			BaseMapper<T> mapper = (BaseMapper<T>)this.sqlSession.getMapper(instance.getMapper());
			return mapper.get(id);
		} catch (InstantiationException | IllegalAccessException e) {
			this.sqlSession.rollback();
			throw new PersistenceException(e);
		} finally {
			this.sqlSession.close();
		}
	}
	
	public void addDivisionToEdition(final TournamentEdition edition, final DivisionRegistration division) {
		Objects.requireNonNull(division);
		Objects.requireNonNull(edition);
		try {
			DivisionRegistrationMapper drm = this.sqlSession.getMapper(DivisionRegistrationMapper.class);
			drm.insert(division, edition);
			
		} finally {
			this.sqlSession.close();
		}
	}

	public TournamentEdition getTournamentByName(final String tournamentName) {
		Objects.requireNonNull(tournamentName);

		TournamentEdition result = this.tournamentPerName.get(tournamentName);
		return result;
	}

	public Collection<TournamentEdition> getAllTournaments() {
		return this.tournamentPerName.values();
	}

	private Event fakeEvent(final LocalDate fakeStartDate) {
		final Event result = new Event();
		result.setStartDate(fakeStartDate);
		return result;
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

	public void refreshDfvNames(List<DfvMvName> dfvNames) {
		this.dfvNames = dfvNames;
	}

	public Set<DfvMvName> getDfvNames(String firstname, String lastname) {
		Set<DfvMvName> names = new HashSet<DfvMvName>();

		for (DfvMvName dfvName : this.dfvNames) {
			if (dfvName.getVorname().equalsIgnoreCase(firstname) && dfvName.getNachname().equalsIgnoreCase(lastname)) {
				names.add(dfvName);
			}
		}

		return names;
	}

	public List<Season> getAllSeasons() {
		try {
			SeasonMapper sm = this.sqlSession.getMapper(SeasonMapper.class);
			return sm.getAll();
		} finally {
			if (this.sqlSession != null) {
				this.sqlSession.close();
			}
		}
	}

	public Season getSeason(final int id) {
		try {
			SeasonMapper sm = this.sqlSession.getMapper(SeasonMapper.class);
			return sm.get(id);
		} finally {
			if (this.sqlSession != null) {
				this.sqlSession.close();
			}
		}
	}

	public Season addSeason(final Season newSeason) {
		Season checkedSeason = Objects.requireNonNull(newSeason);
		try {
			SeasonMapper mapper = this.sqlSession.getMapper(checkedSeason.getMapper());
			mapper.insert(checkedSeason);
			this.sqlSession.commit();
			return checkedSeason;
		} catch (PersistenceException pe) {
			this.sqlSession.rollback();
			throw pe;
		} finally {

			if (this.sqlSession != null) {
				this.sqlSession.close();
			}
		}
	}

	public boolean updateSeason(final Season updSeason) {
		boolean result = false;
		Objects.requireNonNull(updSeason);
		int updateCount = 0;
		try {
			SeasonMapper mapper = this.sqlSession.getMapper(updSeason.getMapper());
			updateCount = mapper.update(updSeason);
			this.sqlSession.commit();
		} catch (PersistenceException pe) {
			this.sqlSession.rollback();
			throw pe;
		} finally {
			if (this.sqlSession != null) {
				this.sqlSession.close();
			}
		}
		result = updateCount == 1;
		return result;
	}

	public void storeUser(User user) {
		// first store dfvPlayer
		this.dfvPlayers.add(user.getDfvPlayer());
		// then store user
		this.users.add(user);
	}

	public User getUserByDfvNr(int dfvNumber) {

		for (User user : this.users) {
			if (user.getDfvPlayer() != null && user.getDfvPlayer().getDfvNumber() == dfvNumber) {
				return user;
			}
		}
		return null;
	}

	public User getUserByEmail(String email) {

		for (User user : this.users) {
			if (user.getEmail().equalsIgnoreCase(email)) {
				return user;
			}
		}
		return null;
	}

	private void fillForTesting() {

		/* USERS */
		User bas = new User();
		bas.setId(1);
		bas.setVersion(1);
		bas.setEmail("bas@knallbude.de");
		bas.setPassword("password");

		Set<User> admins = new HashSet<User>();
		admins.add(bas);

		User basil = new User();
		basil.setId(2);
		basil.setVersion(1);
		basil.setEmail("kaffee@trinkr.com");
		basil.setPassword("password2");

		Set<User> altAdmins = new HashSet<User>();
		altAdmins.add(basil);

		/* SEASONS */
		Season season16 = new Season();
		season16.setId(0);
		season16.setSurface(Surface.TURF);
		season16.setYear(2016);
		season16.setPlusOneYear(false);

		Season season1516 = new Season();
		season1516.setId(1);
		season1516.setSurface(Surface.GYM);
		season1516.setYear(2015);
		season1516.setPlusOneYear(true);

		/* LOCATIONS */
		Location loc1 = new Location();
		loc1.setId(0);
		loc1.setCity("Berlin");
		loc1.setCountry("DE");
		loc1.setStreet("Weserstr. 37");
		loc1.setZipCode(12045);
		loc1.setAdditionalInfo("Nicht abbiegen");

		Location loc2 = new Location();
		loc2.setId(1);
		loc2.setCity("Marburg");
		loc2.setCountry("DE");
		loc2.setStreet("Afföllerwiesen 2");
		loc2.setZipCode(12345);

		/* 4 FERKEL */
		TournamentFormat tf = new TournamentFormat();
		tf.setId(1);
		tf.setName("4 Ferkel");
		tf.setAdmins(admins);
		tf.setDescription(
				"Das 4 Ferkel ist ein Turnier der Superlative. Nicht nur alteingesessene Hasen kommen ins Schwärmen, wenn sie auf die Afföllerwiesen im schönen Marburg auflaufen.\n\n Ein Genuss für die ganze Familie");
		TournamentEditionSingle te = new TournamentEditionSingle();
		te.setId(0);
		te.setSeason(season16);
		te.setTournamentFormat(tf);
		te.setRegistrationStart(LocalDate.parse("2015-10-01"));
		te.setRegistrationEnd(LocalDate.of(2016, 4, 30));
		te.setOrganizerName("Hässlicher Sportverband");
		te.setOrganizerEmail("info@ferkels.de");
		te.setOrganizerPhone("0211/123123123");

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
		e.setFeePerPlayer(13);
		e.setFeePerTeam(50);

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
		tel.setId(1);
		tel.setAlternativeName("32. A-Relegation");
		tel.setAlternativeMatchdayName("Spültag");
		tel.setSeason(season16);
		tel.setTournamentFormat(tf);
		tel.setRegistrationStart(LocalDate.of(2016, 1, 2));
		tel.setRegistrationEnd(LocalDate.of(2016, 3, 30));
		tel.setOrganizerName("Deutscher Frisbeesport Verband e.V.");
		tel.setOrganizerEmail("info@frisbeesportverband.de");
		tel.setOrganizerPhone("0211/123123123");

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
		e1.setLocalOrganizerName("Bas Trapp");
		e1.setLocalOrganizerEmail("bas@ultical.com");
		e1.setLocalOrganizerPhone("(030) 577 0692815");

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
		tel.setId(2);
		tel.setSeason(season1516);
		tel.setTournamentFormat(tf);
		// tel.setRegistrationStart(new LocalDate(2015, 11, 12));
		// tel.setRegistrationStop(new LocalDate(2015, 12, 24));
		tel.setOrganizerName("Boris und Walter");
		tel.setOrganizerEmail("kryptisch@donttrackme.kr");
		tel.setOrganizerPhone("-");
		tel.setFeePerGuest(16);
		tel.setFeePerPlayer(20);
		tel.setFeePerTeam(100);

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
		e1.setStartDate(LocalDate.of(2015, 1, 13));
		e1.setEndDate(LocalDate.of(2015, 1, 14));
		e1.setLocation(loc1);
		e1.setTournamentEdition(tel);
		e1.setLocalOrganizerName("Bas Trapp");
		e1.setLocalOrganizerEmail("bas@ultical.com");
		e1.setLocalOrganizerPhone("(030) 577 0692815");
		e1.setFeePerGuest(5);
		e1.setFeePerPlayer(9);
		e1.setFeePerTeam(40);
		e1.setFeePerLunch(3);
		e1.setFeePerDinner(14);

		e2 = new Event();
		e2.setId(5);
		e2.setMatchdayNumber(2);
		e2.setStartDate(LocalDate.of(2016, 2, 3));
		e2.setEndDate(LocalDate.of(2016, 2, 4));
		e2.setLocation(loc2);
		e2.setTournamentEdition(tel);
		e2.setAdmins(altAdmins);
		e2.setLocalOrganizerName("Erdferkel Marburg e.V.");
		e2.setLocalOrganizerEmail("ferkel@haesslich.de");
		e2.setFeePerBreakfast(5);
		e2.setFeePerPlayer(11);
		e2.setFeePerTeam(50);

		Set<Event> winterligaEvents = new HashSet<Event>();
		winterligaEvents.add(e1);
		winterligaEvents.add(e2);
		// tel.setEvents(winterligaEvents);

		this.tournamentPerName.put(tf.getName(), te);
		this.events.add(e1);
		this.events.add(e2);

	}
}
