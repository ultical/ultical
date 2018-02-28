package de.ultical.backend.jobs;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.model.DivisionAge;
import de.ultical.backend.model.DivisionConfirmation;
import de.ultical.backend.model.DivisionConfirmationTeams;
import de.ultical.backend.model.DivisionRegistration;
import de.ultical.backend.model.DivisionRegistration.DivisionRegistrationStatus;
import de.ultical.backend.model.DivisionRegistrationPlayers;
import de.ultical.backend.model.DivisionRegistrationTeams;
import de.ultical.backend.model.Event;
import de.ultical.backend.model.EventTravelCompensation;
import de.ultical.backend.model.GeoCoordinates;
import de.ultical.backend.model.Location;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.Team;
import de.ultical.backend.model.TeamRegistration;
import de.ultical.backend.services.GeocoderService;

public class TravelCompensationCalculatorTest {

	@Mock
	DataStore dStore;
	@Mock
	DataStore dStoreNoEventLoc;
	@Mock
	DataStore dStoreEventNoMainLoc;
	@Mock
	DataStore dStoreHatTournament;
	@Mock
	DataStore dStoreJuniorEvent;
	@Mock
	GeocoderService geocoder;
	private TravelCompensationCalculator calcUnderTest;
	private TravelCompensationCalculator calcUnderTestNoEventLoc;
	private TravelCompensationCalculator cutEventNoMainLoc;
	private TravelCompensationCalculator cutHatTournament;
	private TravelCompensationCalculator cutJunior;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		Event event = new Event();
		Location eventLocation = this.buildLocation("Berlin");
		event.setLocations(Collections.singletonList(eventLocation));
		when(this.dStore.getEventsEndedAtDate(any(LocalDate.class))).thenReturn(Collections.singletonList(event));
		DivisionConfirmation dc = new DivisionConfirmationTeams();
		event.setDivisionConfirmations(Collections.singletonList(dc));
		DivisionRegistrationTeams drt = new DivisionRegistrationTeams();
		drt.setDivisionAge(DivisionAge.REGULAR);
		dc.setDivisionRegistration(drt);
		drt.setRegisteredTeams(
				Arrays.asList(this.buildRegistration(this.buildRoster("Potsdam"), DivisionRegistrationStatus.CONFIRMED),
						this.buildRegistration(this.buildRoster("Hamburg"), DivisionRegistrationStatus.CONFIRMED)));
		this.calcUnderTest = new TravelCompensationCalculator(this.dStore, this.geocoder, new UltiCalConfig());

		when(this.geocoder.getCoordinates(any(Location.class))).thenReturn(new GeoCoordinates(50, 13),
				new GeoCoordinates(51.4, 13.99), new GeoCoordinates(48.7, 12.1));

		when(this.dStoreNoEventLoc.getEventsEndedAtDate(any(LocalDate.class)))
				.thenReturn(Collections.singletonList(new Event()));
		this.calcUnderTestNoEventLoc = new TravelCompensationCalculator(dStoreNoEventLoc, geocoder,
				new UltiCalConfig());

		Event eventNoMainLoc = new Event();
		Location noMain = this.buildLocation("Foobar");
		noMain.setMain(false);
		eventNoMainLoc.setLocations(Collections.singletonList(noMain));
		when(this.dStoreEventNoMainLoc.getEventsEndedAtDate(any(LocalDate.class)))
				.thenReturn(Collections.singletonList(eventNoMainLoc));
		this.cutEventNoMainLoc = new TravelCompensationCalculator(dStoreEventNoMainLoc, geocoder, new UltiCalConfig());

		Event hatTournament = new Event();
		hatTournament.setLocations(Collections.singletonList(this.buildLocation("Somewhere")));
		DivisionConfirmation dcHat = new DivisionConfirmationTeams();
		DivisionRegistration drHat = new DivisionRegistrationPlayers();
		drHat.setDivisionAge(DivisionAge.REGULAR);
		dcHat.setDivisionRegistration(drHat);
		hatTournament.setDivisionConfirmations(Collections.singletonList(dcHat));
		when(this.dStoreHatTournament.getEventsEndedAtDate(any(LocalDate.class)))
				.thenReturn(Collections.singletonList(hatTournament));
		this.cutHatTournament = new TravelCompensationCalculator(dStoreHatTournament, geocoder, new UltiCalConfig());

		Event juniorEvent = new Event();
		juniorEvent.setLocations(Collections.singletonList(this.buildLocation("Berlin")));
		DivisionRegistration drU14 = new DivisionRegistrationTeams();
		drU14.setDivisionAge(DivisionAge.U14);
		DivisionRegistration drU17 = new DivisionRegistrationTeams();
		drU17.setDivisionAge(DivisionAge.U17);
		DivisionRegistration drU20 = new DivisionRegistrationTeams();
		drU20.setDivisionAge(DivisionAge.U20);
		DivisionRegistration drU23 = new DivisionRegistrationTeams();
		drU23.setDivisionAge(DivisionAge.U23);
		juniorEvent.setDivisionConfirmations(Arrays.asList(drU14, drU17, drU20, drU23).stream().map(r -> {
			DivisionConfirmation dcIntern = new DivisionConfirmationTeams();
			dcIntern.setDivisionRegistration(r);
			return dcIntern;
		}).collect(Collectors.toList()));
		when(this.dStoreJuniorEvent.getEventsEndedAtDate(any(LocalDate.class)))
				.thenReturn(Collections.singletonList(juniorEvent));
		this.cutJunior = new TravelCompensationCalculator(dStoreJuniorEvent, geocoder, new UltiCalConfig());
	}

	private TeamRegistration buildRegistration(final Roster roster, DivisionRegistrationStatus stat) {
		TeamRegistration result = new TeamRegistration();
		result.setRoster(roster);
		result.setStatus(stat);
		return result;
	}

	private Roster buildRoster(final String city) {
		final Roster result = new Roster();
		Team t = new Team();
		result.setTeam(t);
		t.setLocation(this.buildLocation(city));
		return result;
	}

	private Location buildLocation(final String city) {
		Location l = new Location();
		l.setCity(city);
		l.setCountry("Deutschland");
		l.setCountryCode("de");
		l.setMain(true);
		return l;
	}

	@Test
	public void test() {
		this.calcUnderTest.calculate();
		verify(this.dStore, times(2)).addNew(any(EventTravelCompensation.class));
		verify(this.geocoder, times(3)).getCoordinates(any(Location.class));
	}

	@Test
	public void testNoEventLoc() {
		this.calcUnderTestNoEventLoc.calculate();
		verify(this.dStoreNoEventLoc, never()).addNew(any(EventTravelCompensation.class));
		verifyZeroInteractions(this.geocoder);
	}

	@Test
	public void testEventNoMainLoc() {
		this.cutEventNoMainLoc.calculate();
		verify(this.dStoreEventNoMainLoc, never()).addNew(any(EventTravelCompensation.class));
		verifyZeroInteractions(this.geocoder);
	}

	@Test
	public void testHatournament() {
		this.cutHatTournament.calculate();
		verify(this.dStoreHatTournament, never()).addNew(any(EventTravelCompensation.class));
		verify(this.geocoder, times(1)).getCoordinates(any(Location.class));
	}

	@Test
	public void testJunior() {
		this.cutJunior.calculate();
		verify(this.dStoreJuniorEvent, never()).addNew(any(EventTravelCompensation.class));
		verify(this.geocoder, times(1)).getCoordinates(any(Location.class));
	}
}
