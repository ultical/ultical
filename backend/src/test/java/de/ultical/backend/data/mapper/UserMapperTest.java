package de.ultical.backend.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.junit.*;

import de.ultical.backend.model.*;
import de.ultical.backend.utils.test.PrepareDBRule;

public class UserMapperTest {

	private static final String UPDATED_PASSWORD = "very secret";
	private static final String EMAIL = "test@ultical.com";
	private static final String PASSWORD = "secret";
	private SqlSession session;
	private User user;
	
	@ClassRule
	public static PrepareDBRule DBRULE = new PrepareDBRule();
	
	@Before
	public void setUp() throws Exception {
		session = DBRULE.getSession();
		user = new User();
		user.setEmail(EMAIL);
		user.setPassword(PASSWORD);
		
		final DfvPlayer player = new DfvPlayer();
		player.setFirstName("Brodie");
		player.setLastName("Smith");
		player.setGender(Gender.MALE);
		player.setDfvNumber("123456");
		player.setBirthDate(LocalDate.of(1979, 1, 25));
		
		DfvPlayerMapper playerMapper = session.getMapper(DfvPlayerMapper.class);
		PlayerMapper pMapper = session.getMapper(PlayerMapper.class);
		pMapper.insert(player);
		playerMapper.insert(player);
		
		user.setDfvPlayer(player);
		session.commit();
	}
	
	public void tearDown() throws Exception {
		DBRULE.closeSession();
		session= null;
	}

	@Test
	public void test() {
		UserMapper userMapper = session.getMapper(UserMapper.class);
		userMapper.insert(user);
		session.commit();
		
		User foundUser = userMapper.get(1);
		assertNotNull(foundUser);
		assertEquals(PASSWORD, foundUser.getPassword());
		assertEquals(EMAIL, foundUser.getEmail());
		assertEquals(1, foundUser.getVersion());
		assertNotNull(foundUser.getDfvPlayer());
		
		/*
		 * test getAll
		 */
		List<User> allUsers = userMapper.getAll();
		assertNotNull(allUsers);
		assertEquals(1, allUsers.size());
		// inserting the user once again, and check if getAll still works.
		userMapper.insert(user);
		allUsers = userMapper.getAll();
		assertNotNull(allUsers);
		assertEquals(2, allUsers.size());
		
		/*
		 * test update of user
		 */
		foundUser.setPassword(UPDATED_PASSWORD);
		final int updateCount = userMapper.update(foundUser);
		assertEquals(1, updateCount);
		foundUser = userMapper.get(1);
		assertNotNull(foundUser);
		assertEquals(UPDATED_PASSWORD, foundUser.getPassword());
		assertEquals(2, foundUser.getVersion());
		
		/*
		 * test delete
		 */
		userMapper.delete(foundUser);
		assertNull(userMapper.get(1));
	}

	@Test(expected = PersistenceException.class)
	public void violateForeignKey() {
		UserMapper userMapper = session.getMapper(UserMapper.class);
		DfvPlayer nonExistingPlayer = new DfvPlayer();
		nonExistingPlayer.setId(1024);
		this.user.setDfvPlayer(nonExistingPlayer);
		userMapper.insert(this.user);
	}
}
