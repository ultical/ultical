package de.ultical.backend.services.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.spi.ThreadPoolExecutorProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ultical.backend.app.UltiCalConfig;
import de.ultical.backend.app.UltiCalConfig.ExternalService;
import de.ultical.backend.model.GeoCoordinates;
import de.ultical.backend.model.Location;
import de.ultical.backend.services.impl.GeocoderServiceImpl.MapQuestLatLong;
import de.ultical.backend.services.impl.GeocoderServiceImpl.MapQuestLocation;
import de.ultical.backend.services.impl.GeocoderServiceImpl.MapQuestResponse;
import de.ultical.backend.services.impl.GeocoderServiceImpl.MapQuestResult;
import io.dropwizard.client.JerseyClientBuilder;

public class GeocoderServiceImplTest {

	private static final String API_ENDPOINT = "https://www.mapquestapi.com/geocoding/v1/address";

	private static final String key = System.getProperty("ultical.mapquest.key");
	
	private GeocoderServiceImpl service;

	private Client mockClient;

	private WebTarget mockTarget;

	private Invocation.Builder mockBuilder;

	@Before
	public void setUp() {
		mockClient = mock(Client.class);
		mockTarget = mock(WebTarget.class);
		mockBuilder = mock(Invocation.Builder.class);
		MapQuestResponse response = new MapQuestResponse();
		MapQuestResult mqr = new MapQuestResult();
		MapQuestLocation mql = new MapQuestLocation();
		MapQuestLatLong ll = new MapQuestLatLong();
		ll.lat = 42f;
		ll.lng = 13f;
		mql.latLng = ll;
		mqr.locations = Collections.singletonList(mql);
		response.results = Collections.singletonList(mqr);

		when(mockClient.target(any(String.class))).thenReturn(mockTarget);
		when(mockTarget.queryParam(any(String.class), any())).thenReturn(mockTarget);
		when(mockTarget.request(any(MediaType.class))).thenReturn(mockBuilder);
		when(mockBuilder.post(any(), Mockito.eq(MapQuestResponse.class))).thenReturn(response);
		UltiCalConfig config = buildConfig("foobar");

		this.service = new GeocoderServiceImpl(mockClient, config);
	}

	private UltiCalConfig buildConfig() {
		return this.buildConfig(key);
	}

	private UltiCalConfig buildConfig(String apiKey) {
		UltiCalConfig config = new UltiCalConfig();
		ExternalService geoCoderService = new ExternalService();
		geoCoderService.setSecret(apiKey);
		geoCoderService.setUrl(API_ENDPOINT);
		config.setGeocoderConfig(geoCoderService);
		return config;
	}

	private Client buildRealClient() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		ExecutorService execService = (new ThreadPoolExecutorProvider("foobar")).getExecutorService();
		MetricRegistry mr = new MetricRegistry();
		Client httpClient = (new JerseyClientBuilder(mr)).using(execService).using(mapper).build("test client");
		return httpClient;
	}

	@Test(timeout = 5000)
	public void testLocationLookup() {
		// this test is only useful if a proper mapquest api key has been set. Thus, if
		// <code>key</code> is null we skip this test to avoid meaningless errors.
		assumeNotNull(key);
		this.service = new GeocoderServiceImpl(this.buildRealClient(), this.buildConfig());
		Location loc = new Location();
		loc.setCity("Potsdam");
		loc.setCountry("Germany");
		GeoCoordinates result = this.service.getCoordinates(loc);
		assertNotNull(result);
		assertEquals(52.400931, result.latitude, 0.000002);
		assertEquals(13.05914, result.longitude, 0.00002);
	}

	@Test(expected = NullPointerException.class)
	public void testLocationNull() {
		this.service.getCoordinates(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCityNull() {
		Location loc = new Location();
		this.service.getCoordinates(loc);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCityEmptyString() {
		Location loc = new Location();
		loc.setCity("   \t");
		this.service.getCoordinates(loc);
	}

	@Test
	public void testLookupMocked() {
		Location loc = new Location();
		loc.setCity("Potsdam");
		GeoCoordinates coords = this.service.getCoordinates(loc);
		assertNotNull(coords);
		assertEquals(42, coords.latitude, 0.001);
		assertEquals(13, coords.longitude, 0.001);
		verify(mockClient).target(eq(API_ENDPOINT));
		verify(mockTarget).queryParam(eq("key"), eq("foobar"));
		verify(mockTarget).queryParam(eq("outFormat"), eq("json"));
		verify(mockTarget).request(eq(MediaType.APPLICATION_JSON_TYPE));
		verify(mockBuilder).post(any(Entity.class),eq(MapQuestResponse.class));
	}

}
