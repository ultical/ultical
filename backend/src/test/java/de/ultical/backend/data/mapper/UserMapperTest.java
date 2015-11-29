package de.ultical.backend.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.List;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;
import org.junit.*;

import de.ultical.backend.model.*;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.jvm.DerbyConnection;
import liquibase.logging.LogLevel;
import liquibase.resource.ClassLoaderResourceAccessor;

public class UserMapperTest {

	private static final String UPDATED_PASSWORD = "very secret";
	private static final String EMAIL = "test@ultical.com";
	private static final String PASSWORD = "secret";
	private static final String USERNAME = "firstUser";
	private static SqlSessionFactory sessionFactory;
	private SqlSession session;
	private User user;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		Liquibase liquibase = null;
		try {
			DriverManager.registerDriver(new EmbeddedDriver());
			Connection dbCon = DriverManager.getConnection("jdbc:derby:memory:test;create=true");
			final DerbyDatabase derbyDatabase = new DerbyDatabase();
			final DatabaseConnection liquibaseConnection = new DerbyConnection(dbCon);
			derbyDatabase.setConnection(liquibaseConnection);
			liquibase = new Liquibase("de/ultical/backend/data/db/db.changelog-1.0.xml",
					new ClassLoaderResourceAccessor(), derbyDatabase);
			liquibase.getLog().setLogLevel(LogLevel.DEBUG);
			liquibase.update((Contexts) null);
		} finally {

		}

		/*
		 * setting up mybatis using the dbconnection defined above.
		 */
		sessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config.xml"),
				"inmemory-test");
	}
	
	@Before
	public void setUp() throws Exception {
		session = sessionFactory.openSession();
		user = new User();
		user.setEmail(EMAIL);
		user.setPassword(PASSWORD);
		user.setUsername(USERNAME);
		
		final DfvPlayer player = new DfvPlayer();
		player.setFirstName("Brodie");
		player.setLastName("Smith");
		player.setGender(Gender.MALE);
		player.setDfvNumber("123456");
		player.setBiography("");
		player.setBirthDate(LocalDate.of(1979, 1, 25));
		
		DfvPlayerMapper playerMapper = session.getMapper(DfvPlayerMapper.class);
		playerMapper.insert(player);
		player.setId(0);
		user.setDfvPlayer(player);
		session.commit();
	}
	
	public void tearDown() throws Exception {
		if (session != null) {
			session.close();
		}
	}

	@Test
	public void test() {
		UserMapper userMapper = session.getMapper(UserMapper.class);
		userMapper.insert(user);
		session.commit();
		
		User foundUser = userMapper.get(1);
		assertNotNull(foundUser);
		assertEquals(USERNAME, foundUser.getUsername());
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
		assertEquals(USERNAME, foundUser.getUsername());
		
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
