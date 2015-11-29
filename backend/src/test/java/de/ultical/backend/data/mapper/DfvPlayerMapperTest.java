package de.ultical.backend.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.List;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;
import org.junit.*;

import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.Gender;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.jvm.DerbyConnection;
import liquibase.logging.LogLevel;
import liquibase.resource.ClassLoaderResourceAccessor;

public class DfvPlayerMapperTest {

	private static SqlSessionFactory sessionFactory;
	private SqlSession session;
	private DfvPlayer dfvPlayer;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (sessionFactory != null) {
			sessionFactory = null;
		}
	}

	@Before
	public void setUp() throws Exception {
		session = sessionFactory.openSession();
		dfvPlayer = new DfvPlayer();
		dfvPlayer.setBiography("I'm famous");
		dfvPlayer.setDfvNumber("123456");
		dfvPlayer.setFirstName("Brodie");
		dfvPlayer.setLastName("Smith");
		dfvPlayer.setGender(Gender.MALE);

		dfvPlayer.setBirthDate(LocalDate.now());
	}

	@After
	public void tearDown() throws Exception {
		if (session != null) {
			session.close();
			session = null;
		}
	}

	@Test
	public void test() {
		DfvPlayerMapper mapper = session.getMapper(DfvPlayerMapper.class);
		mapper.insert(dfvPlayer);
		session.commit();
		List<DfvPlayer> allPlayers = mapper.getAll();
		assertNotNull(allPlayers);
		assertEquals(1, allPlayers.size());

		final DfvPlayer foundPlayer = mapper.get(0);
		assertNotNull(foundPlayer);
		assertEquals(1, foundPlayer.getVersion());
		assertEquals(0, foundPlayer.getId());
		assertNotNull(foundPlayer.getFirstName());
		assertEquals(dfvPlayer.getGender(), foundPlayer.getGender());
		assertEquals(dfvPlayer.getBirthDate(), foundPlayer.getBirthDate());
		assertEquals(dfvPlayer.getFirstName(), foundPlayer.getFirstName());
		assertEquals(dfvPlayer.getLastName(), foundPlayer.getLastName());
		assertEquals(dfvPlayer.getBiography(), foundPlayer.getBiography());

		/*
		 * test update of players
		 */
		foundPlayer.setBiography("I was famous");
		Integer shouldBeOne = mapper.update(foundPlayer);
		assertEquals(Integer.valueOf(1),shouldBeOne);
		session.commit();
		DfvPlayer updatedPlayer = mapper.get(0);
		checkUpdatedPlayer(updatedPlayer);

		// update again, but using the foundPlayer instance, which has the wrong
		// version set by now.
		foundPlayer.setBiography("I am famous again");
		Integer shouldBeZero= mapper.update(foundPlayer);
		assertEquals(Integer.valueOf(0),shouldBeZero);
		// as update is supposed to not succeed, we can check the player using
		// the same checks as after the first update
		updatedPlayer = mapper.get(0);
		checkUpdatedPlayer(updatedPlayer);
	}

	private void checkUpdatedPlayer(final DfvPlayer updatedPlayer) {
		assertNotNull(updatedPlayer);
		assertEquals(0, updatedPlayer.getId());
		assertEquals(2, updatedPlayer.getVersion());
		assertEquals("I was famous", updatedPlayer.getBiography());
		assertEquals(dfvPlayer.getGender(), updatedPlayer.getGender());
		assertEquals(dfvPlayer.getBirthDate(), updatedPlayer.getBirthDate());
		assertEquals(dfvPlayer.getFirstName(), updatedPlayer.getFirstName());
		assertEquals(dfvPlayer.getLastName(), updatedPlayer.getLastName());
	}

}
