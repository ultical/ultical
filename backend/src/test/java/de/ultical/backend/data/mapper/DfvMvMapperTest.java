package de.ultical.backend.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.apache.ibatis.exceptions.PersistenceException;
import org.junit.*;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.utils.test.PrepareDBRule;

public class DfvMvMapperTest {

	private static final String NACHNAME = "Dfv-Mv Nachname";

	private static final String VORNAME = "Dfv-Mv Vorname";

	@ClassRule
	public static PrepareDBRule DBRULE = new PrepareDBRule();

	private DfvMvName dfvMvName;
	private DfvMvNameMapper mapper;

	@Before
	public void setUp() throws Exception {
		dfvMvName = new DfvMvName();
		dfvMvName.setDfvnr(1234567);
		dfvMvName.setVorname(VORNAME);
		dfvMvName.setNachname(NACHNAME);
		dfvMvName.setDse(true);

		mapper = DBRULE.getSession().getMapper(DfvMvNameMapper.class);
	}

	@Test
	public void test() {
		Integer id = mapper.insert(dfvMvName);
		assertNotNull(id);
		DfvMvName found = mapper.get(1234567);
		assertNotNull(found);
		assertEquals(VORNAME, found.getVorname());
		assertEquals(NACHNAME, found.getNachname());
		assertEquals(true, found.isDse());

		DfvMvName other = new DfvMvName();
		other.setDfvnr(1234568);
		other.setVorname(VORNAME);
		other.setNachname(NACHNAME);
		mapper.insert(other);

		other = new DfvMvName();
		other.setDfvnr(1234569);
		other.setVorname(VORNAME);
		other.setNachname(NACHNAME);
		mapper.insert(other);

		List<DfvMvName> names = mapper.getAll();
		assertNotNull(names);
		assertEquals(3, names.size());

		mapper.deleteAll();
		assertNull(mapper.get(1234567));
		assertNull(mapper.get(1234568));
		assertNull(mapper.get(1234569));
	}

	@Test(expected = PersistenceException.class)
	public void testPrimaryKeyConstraintViolation() throws Exception {
		try {
			this.mapper.insert(dfvMvName);
			this.mapper.insert(dfvMvName);
		} finally {
			// avoids an exception in case this test is run before the test()
			// method.
			this.mapper.deleteAll();
		}
	}

}
