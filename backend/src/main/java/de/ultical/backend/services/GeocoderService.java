package de.ultical.backend.services;

import de.ultical.backend.model.GeoCoordinates;
import de.ultical.backend.model.Location;

public interface GeocoderService {
	GeoCoordinates getCoordinates(Location loc);
}
