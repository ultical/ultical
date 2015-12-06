package de.ultical.backend.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.*;

import de.ultical.backend.model.*;
import de.ultical.backend.utils.test.PrepareDBRule;

public class UnregisteredPlayerMapperTest {

	private static final String EMAIL = "brodie@ultical.com";
	private static final String LASTNAME = "Smith";
	private static final String FIRSTNAME = "Brodie";
	private UnregisteredPlayer unregisteredPlayer;
	private PlayerMapper mapper;
	private UnregisteredPlayerMapper unRegMapper;

	@ClassRule
	public static PrepareDBRule DBRULE = new PrepareDBRule();

	@Before
	public void setUp() throws Exception {

		this.unregisteredPlayer = new UnregisteredPlayer();
		this.unregisteredPlayer.setFirstName(FIRSTNAME);
		this.unregisteredPlayer.setLastName(LASTNAME);
		this.unregisteredPlayer.setEmail(EMAIL);
		this.unregisteredPlayer.setGender(Gender.MALE);
		this.mapper = DBRULE.getSession().getMapper(this.unregisteredPlayer.getMapper());
		this.unRegMapper = DBRULE.getSession().getMapper(UnregisteredPlayerMapper.class);
	}

	@After
	public void after() throws Exception {
		DBRULE.closeSession();
	}

	@Test
	public void test() {
		this.mapper.insert(unregisteredPlayer);
		this.unRegMapper.insert(unregisteredPlayer);
		final Player foundPlayer = this.mapper.get(1);
		assertNotNull(foundPlayer);
		assertTrue(foundPlayer instanceof UnregisteredPlayer);
		assertEquals(FIRSTNAME, foundPlayer.getFirstName());
		assertEquals(LASTNAME, foundPlayer.getLastName());
		assertEquals(EMAIL, foundPlayer.getEmail());
		assertEquals(Gender.MALE, foundPlayer.getGender());
		assertEquals(1, foundPlayer.getVersion());

		/*
		 * test getAll
		 */
		this.mapper.insert(unregisteredPlayer);
		this.mapper.insert(unregisteredPlayer);
		List<Player> allPlayers = this.mapper.getAll();
		assertNotNull(allPlayers);
		assertEquals(3, allPlayers.size());

		/*
		 * test update
		 */
		foundPlayer.setFirstName("Foo");
		foundPlayer.setLastName("Bar");

		final int updateCount = this.mapper.update(foundPlayer);
		this.unRegMapper.update((UnregisteredPlayer) foundPlayer);
		assertEquals(1, updateCount);
		Player updatedPlayer = this.mapper.get(1);
		assertNotNull(updatedPlayer);
		assertTrue(updatedPlayer instanceof UnregisteredPlayer);
		assertEquals(2, updatedPlayer.getVersion());
		assertEquals(Gender.MALE, updatedPlayer.getGender());
		assertEquals(EMAIL, updatedPlayer.getEmail());
		assertEquals("Foo", updatedPlayer.getFirstName());
		assertEquals("Bar", updatedPlayer.getLastName());
		// second update should "fail" as version has been incremented
		assertEquals(Integer.valueOf(0), this.mapper.update(foundPlayer));

		allPlayers = this.mapper.getAll();
		assertNotNull(allPlayers);
		assertEquals(3, allPlayers.size());

		/*
		 * test delete
		 */
		this.mapper.delete(foundPlayer);
		assertNull(this.mapper.get(1));
		assertNotNull(this.mapper.get(2));
		assertNotNull(this.mapper.get(3));
	}

}
