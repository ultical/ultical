package de.ultical.backend.jobs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.data.DataStore;
import de.ultical.backend.exception.TravelCompensationException;
import de.ultical.backend.model.DivisionAge;
import de.ultical.backend.model.DivisionConfirmation;
import de.ultical.backend.model.DivisionRegistration;
import de.ultical.backend.model.DivisionRegistration.DivisionRegistrationStatus;
import de.ultical.backend.model.DivisionRegistrationTeams;
import de.ultical.backend.model.Event;
import de.ultical.backend.model.EventTravelCompensation;
import de.ultical.backend.model.GeoCoordinates;
import de.ultical.backend.model.Location;
import de.ultical.backend.model.Roster;
import de.ultical.backend.services.GeocoderService;

public class TravelCompensationCalculator {

	private static final Logger LOGGER = LoggerFactory.getLogger(TravelCompensationCalculator.class);

	private final DataStore data;
	private final GeocoderService geocoder;
	private final UltiCalConfig.TravelCompensationFees fees;

	@Inject
	public TravelCompensationCalculator(DataStore dStore, GeocoderService geoService, UltiCalConfig config) {
		this.data = dStore;
		this.geocoder = geoService;
		this.fees = config.getTravelFees();
	}

	public void calculate() {
		final LocalDate endDate = LocalDate.now().minusDays(1);
		LOGGER.debug("Calculation travel compensation for all events, that ended at: {}", endDate.toString());
		Optional<List<Event>> eventsToConsider = Optional.ofNullable(this.data.getEventsEndedAtDate(endDate));
		eventsToConsider.ifPresent(l -> l.forEach(e -> this.calculate(e)));
	}

	private void calculate(final Event e) {
		try {
			final Location loc = this.getMainLocation(e);
			final GeoCoordinates eventCoords = this.geocoder.getCoordinates(loc);
			Optional.ofNullable(e.getDivisionConfirmations()).orElse(Collections.emptyList()).stream()
					.map(DivisionConfirmation::getDivisionRegistration)
					.filter(r -> r.getDivisionAge() == DivisionAge.REGULAR || r.getDivisionAge() == DivisionAge.MASTERS
							|| r.getDivisionAge() == DivisionAge.GRANDMASTERS)
					.forEach(r -> calculate(e, r, eventCoords));
		} catch (TravelCompensationException e1) {
			String message = String.format("Travel compensation could not be calculated for event %d", e.getId());
			LOGGER.warn(message, e1);
		}
	}

	private class ComputationWrapper {
		private final Event e;
		private final DivisionRegistrationTeams tr;
		private final GeoCoordinates eventCoords;
		private final AtomicInteger accumulatedDistance;

		public ComputationWrapper(Event e, DivisionRegistrationTeams tr, GeoCoordinates eCoords,
				AtomicInteger accumDist) {
			this.e = e;
			this.tr = tr;
			this.eventCoords = eCoords;
			this.accumulatedDistance = accumDist;
		}

		private EventTravelCompensation provideCompensation(final Roster r) {
			GeoCoordinates coords = TravelCompensationCalculator.this.geocoder
					.getCoordinates(r.getTeam().getLocation());
			final double distance = eventCoords.distanceTo(coords); // distance in meters
			final int distanceRounded = Double.valueOf(distance).intValue();
			final Integer distanceKm = Integer.valueOf(distanceRounded / 1000);
			EventTravelCompensation etc = new EventTravelCompensation();
			etc.setEvent(e);
			etc.setRoster(r);
			etc.setDivisionRegistration(tr);
			etc.setDistance(distanceKm);
			accumulatedDistance.addAndGet(distanceKm);
			return etc;
		}
	}

	private void calculate(final Event e, final DivisionRegistration dr, final GeoCoordinates eventCoords) {
		if (dr instanceof DivisionRegistrationTeams) {
			final DivisionRegistrationTeams teamReg = (DivisionRegistrationTeams) dr;
			List<Roster> teams = Optional.ofNullable(teamReg.getRegisteredTeams()).orElse(Collections.emptyList())
					.stream().filter(tr -> tr.getStatus() == DivisionRegistrationStatus.CONFIRMED)
					.map(tr -> tr.getRoster()).collect(Collectors.toList());
			AtomicInteger accumulatedDistance = new AtomicInteger(0);
			ComputationWrapper cw = new ComputationWrapper(e, teamReg, eventCoords, accumulatedDistance);
			List<EventTravelCompensation> compensations = teams.stream().map(cw::provideCompensation)
					.collect(Collectors.toList());
			Double averageDistance = BigDecimal.valueOf(accumulatedDistance.get())
					.divide(BigDecimal.valueOf(compensations.size()), 3, BigDecimal.ROUND_HALF_UP).doubleValue();
			compensations.stream().forEach(c -> {
				c.setFee(Double.valueOf((c.getDistance().doubleValue() - averageDistance) * this.fees.getOutdoor())
						.floatValue());
				this.data.addNew(c);
			});
		} else {
			LOGGER.info("Event {} is an hat tournament, hence we don't compute any compensation stuff", e.getId());
		}

	}

	private Location getMainLocation(final Event e) throws TravelCompensationException {
		Optional<List<Location>> locs = Optional.ofNullable(e.getLocations());
		final Optional<Location> result = locs
				.orElseThrow(() -> TravelCompensationException.noLocationsFound(e.getId())).stream()
				.filter(l -> l.isMain()).findFirst();
		return result.orElseThrow(() -> TravelCompensationException.noMainLocationForEvent(e.getId()));
	}
}
