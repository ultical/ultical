package de.ultical.backend.services.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;

import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.model.GeoCoordinates;
import de.ultical.backend.model.Location;
import de.ultical.backend.services.GeocoderService;

public class GeocoderServiceImpl implements GeocoderService {

	private static class MapQuestOptions {
		@SuppressWarnings("unused")
		public boolean thumbMaps = false;
		@SuppressWarnings("unused")
		public int maxResults = 1;
	}

	private static class MapQuestRequest {
		@SuppressWarnings("unused")
		public String location;
		@SuppressWarnings("unused")
			public MapQuestOptions options = new MapQuestOptions();

	}

	static class MapQuestResponse {
		public List<MapQuestResult> results;
	}

	@SuppressWarnings("unused")
	static class MapQuestLocation {
		public String street, adminArea6, adminArea5, adminArea4, adminArea3, adminArea2, adminArea1, adminArea6Type,
				adminArea5Type, adminArea4Type, adminArea3Type, adminArea2Type, adminArea1Type, postalCode,
				geocodeQualityCode, geocodeQuality, sideOfStreet, linkId, uknownInput, type;
		public boolean dragPoint;
		public MapQuestLatLong latLng, displayLatLng;

	}

	static class MapQuestResult {
		@SuppressWarnings("unused")
		public Map<String, String> providedLocation;
		public List<MapQuestLocation> locations;

	}

	static class MapQuestLatLong {
		public float lat, lng;
	}

	private final Client httpClient;
	private final UltiCalConfig.ExternalService endpointConfig;

	@Inject
	public GeocoderServiceImpl(final Client client, final UltiCalConfig config) {
		this.httpClient = client;
		this.endpointConfig = config.getGeocoderConfig();
	}

	@Override
	public GeoCoordinates getCoordinates(Location loc) {
		Objects.requireNonNull(loc);
		if (loc.getCity() == null || loc.getCity().trim().isEmpty()) {
			//we have a reasonable default for country but not for the city.
			throw new IllegalArgumentException("City must not be null or empty");
		}
		
		GeoCoordinates result = null;
		MapQuestRequest body = new MapQuestRequest();
		body.location = loc.getCity() + ", "
				+ (loc.getCountry() != null && !loc.getCountry().trim().isEmpty() ? loc.getCountry() : "de");
		/**
		 * MapQuest does not allow or cannot handle gzip-ed content. Hence, we have to
		 * disable gzip-compression for this request. During request-processing any
		 * eventually set content-encoding header is overwritten by the set
		 * <code>Entity</code>'s Variant-encoding setting. Therefore, we use this
		 * somewhat unusual way of building an entity.
		 */
		Entity<MapQuestRequest> entity = Entity.entity(body, new Variant(MediaType.APPLICATION_JSON_TYPE, null, null, "identity"));

		final MapQuestResponse response = this.httpClient.target(this.endpointConfig.getUrl())
				.queryParam("key", this.endpointConfig.getSecret()).queryParam("outFormat", "json")
				.request(MediaType.APPLICATION_JSON_TYPE).post(entity, MapQuestResponse.class);
		if (response != null) {
			MapQuestLatLong ll = response.results.get(0).locations.get(0).latLng;
			result = new GeoCoordinates(ll.lat, ll.lng);

		}
		return result;
	}

}
