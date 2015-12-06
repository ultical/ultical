package de.ultical.backend.data.mapper;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.*;

import de.ultical.backend.model.Location;
import de.ultical.backend.utils.test.PrepareDBRule;

public class LocationMapperTest {

	private static final double LONG = 45.342343;

	private static final double LAT = 51.343434;

	private static final String ADDITIONAL_INFO = "le Karli";

	private static final int ZIPCODE = 14482;

	private static final String COUNTRY = "Germany";

	private static final String STREET = "Karl Liebknecht Str.";

	private static final String CITY = "Potsdam";

	@ClassRule
	public static PrepareDBRule DBRULE= new PrepareDBRule();
	
	private Location location;
	private LocationMapper mapper;
	
	@Before
	public void setUp() throws Exception {
		
		location = new Location();
		location.setCity(CITY);
		location.setStreet(STREET);
		location.setZipCode(ZIPCODE);
		location.setCountry(COUNTRY);
		location.setAdditionalInfo(ADDITIONAL_INFO);
		location.setLatitude(LAT);
		location.setLongitude(LONG);
		
		mapper = DBRULE.getSession().getMapper(location.getMapper());
	}
	
	@After
	public void tearDown() throws Exception {
		DBRULE.closeSession();
	}

	@Test
	public void test() {
		assertEquals(0, location.getId());
		mapper.insert(location);
		DBRULE.getSession().commit();
		assertEquals(1, location.getId());
		
		Location readLocation = mapper.get(1);
		assertNotNull(readLocation);
		assertEquals(CITY, readLocation.getCity());
		assertEquals(STREET, readLocation.getStreet());
		assertEquals(ZIPCODE, readLocation.getZipCode());
		assertEquals(ADDITIONAL_INFO, readLocation.getAdditionalInfo());
		assertEquals(LAT, readLocation.getLatitude(), 0);
		assertEquals(LONG, readLocation.getLongitude(), 0);
		assertEquals(1, readLocation.getVersion());
		
		int updCount = mapper.update(this.location);
		assertEquals(0, updCount);
		
		readLocation.setCity("Berlin");
		updCount = mapper.update(readLocation);
		assertEquals(1, updCount);
		
		readLocation = mapper.get(1);
		assertNotNull(readLocation);
		assertEquals(2, readLocation.getVersion());
		assertEquals("Berlin", readLocation.getCity());
		assertEquals(ZIPCODE, readLocation.getZipCode());
		
		mapper.insert(readLocation);
		DBRULE.getSession().commit();
		assertEquals(2, readLocation.getId());
		
		List<Location> allLocations = mapper.getAll();
		assertNotNull(allLocations);
		assertEquals(2, allLocations.size());
		
		mapper.delete(readLocation);
		assertNull(mapper.get(2));
		DBRULE.getSession().commit();
		
		allLocations = mapper.getAll();
		assertNotNull(allLocations);
		assertEquals(1, allLocations.size());
	}

}
