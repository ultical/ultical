package de.ultical.backend.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.*;

import de.ultical.backend.model.*;
import de.ultical.backend.utils.test.PrepareDBRule;

public class DfvPlayerMapperTest {

	private DfvPlayer dfvPlayer;

	@ClassRule
	public static PrepareDBRule dbRule = new PrepareDBRule();

	@Before
	public void setUp() throws Exception {
		dbRule.getSession();
		dfvPlayer = new DfvPlayer();
		dfvPlayer.setDfvNumber("123456");
		dfvPlayer.setFirstName("Brodie");
		dfvPlayer.setLastName("Smith");
		dfvPlayer.setGender(Gender.MALE);

		dfvPlayer.setBirthDate(LocalDate.now());
	}

	@After
	public void tearDown() throws Exception {
		dbRule.closeSession();
	}

	@Test
	public void test() {
		PlayerMapper playerMapper = dbRule.getSession().getMapper(PlayerMapper.class);
		DfvPlayerMapper mapper = dbRule.getSession().getMapper(DfvPlayerMapper.class);
		playerMapper.insert(dfvPlayer);
		final int insertedId = dfvPlayer.getId();
		mapper.insert(dfvPlayer);
		dbRule.getSession().commit();
		List<Player> allPlayers = playerMapper.getAll();
		assertNotNull(allPlayers);
		assertEquals(1, allPlayers.size());
		assertTrue(allPlayers.get(0) instanceof DfvPlayer);

		final DfvPlayer foundPlayer = (DfvPlayer) playerMapper.get(insertedId);
		assertNotNull(foundPlayer);
		assertEquals(1, foundPlayer.getVersion());
		assertEquals(insertedId, foundPlayer.getId());
		assertNotNull(foundPlayer.getFirstName());
		assertEquals(dfvPlayer.getGender(), foundPlayer.getGender());
		assertEquals(dfvPlayer.getBirthDate(), foundPlayer.getBirthDate());
		assertEquals(dfvPlayer.getFirstName(), foundPlayer.getFirstName());
		assertEquals(dfvPlayer.getLastName(), foundPlayer.getLastName());

		/*
		 * test update of players
		 */
		
		Integer shouldBeOne = playerMapper.update(foundPlayer);
		mapper.update(foundPlayer);
		assertEquals(Integer.valueOf(1),shouldBeOne);
		dbRule.getSession().commit();
		DfvPlayer updatedPlayer = (DfvPlayer) playerMapper.get(insertedId);
		checkUpdatedPlayer(updatedPlayer);

		// update again, but using the foundPlayer instance, which has the wrong
		// version set by now.
		Integer shouldBeZero= playerMapper.update(foundPlayer);
		assertEquals(Integer.valueOf(0),shouldBeZero);
		// as update is supposed to not succeed, we can check the player using
		// the same checks as after the first update
		updatedPlayer = (DfvPlayer) playerMapper.get(insertedId);
		checkUpdatedPlayer(updatedPlayer);
	}

	private void checkUpdatedPlayer(final DfvPlayer updatedPlayer) {
		assertNotNull(updatedPlayer);
		assertEquals(2, updatedPlayer.getVersion());
		assertEquals(dfvPlayer.getGender(), updatedPlayer.getGender());
		assertEquals(dfvPlayer.getBirthDate(), updatedPlayer.getBirthDate());
		assertEquals(dfvPlayer.getFirstName(), updatedPlayer.getFirstName());
		assertEquals(dfvPlayer.getLastName(), updatedPlayer.getLastName());
	}

}
