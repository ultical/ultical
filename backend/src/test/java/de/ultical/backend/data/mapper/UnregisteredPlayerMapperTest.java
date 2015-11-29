package de.ultical.backend.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;
import org.junit.*;

import de.ultical.backend.model.Gender;
import de.ultical.backend.model.UnregisteredPlayer;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.jvm.DerbyConnection;
import liquibase.logging.LogLevel;
import liquibase.resource.ClassLoaderResourceAccessor;

public class UnregisteredPlayerMapperTest {

	private static final String EMAIL = "brodie@ultical.com";
	private static final String LASTNAME = "Smith";
	private static final String FIRSTNAME = "Brodie";
	private static SqlSessionFactory sessionFactory;
	private SqlSession session;
	private UnregisteredPlayer unregisteredPlayer;
	private UnregisteredPlayerMapper mapper;

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
		this.unregisteredPlayer = new UnregisteredPlayer();
		this.unregisteredPlayer.setFirstName(FIRSTNAME);
		this.unregisteredPlayer.setLastName(LASTNAME);
		this.unregisteredPlayer.setEmail(EMAIL);
		this.unregisteredPlayer.setGender(Gender.MALE);
		this.mapper = session.getMapper(UnregisteredPlayerMapper.class);
	}
	
	@After
	public void after() throws Exception {
		if (session != null) {
			session.close();
		}
	}

	@Test
	public void test() {
		this.mapper.insert(unregisteredPlayer);
		final UnregisteredPlayer foundPlayer = this.mapper.get(1);
		assertNotNull(foundPlayer);
		assertEquals(FIRSTNAME, foundPlayer.getFirstName());
		assertEquals(LASTNAME,foundPlayer.getLastName());
		assertEquals(EMAIL,foundPlayer.getEmail());
		assertEquals(Gender.MALE, foundPlayer.getGender());
		assertEquals(1, foundPlayer.getVersion());
		
		/*
		 * test getAll
		 */
		this.mapper.insert(unregisteredPlayer);
		this.mapper.insert(unregisteredPlayer);
		List<UnregisteredPlayer> allPlayers = this.mapper.getAll();
		assertNotNull(allPlayers);
		assertEquals(3,allPlayers.size());
		
		/*
		 * test update
		 */
		foundPlayer.setFirstName("Foo");
		foundPlayer.setLastName("Bar");
		final int updateCount = this.mapper.update(foundPlayer);
		assertEquals(1, updateCount);
		UnregisteredPlayer updatedPlayer = this.mapper.get(1);
		assertNotNull(updatedPlayer);
		assertEquals(2, updatedPlayer.getVersion());
		assertEquals(Gender.MALE, updatedPlayer.getGender());
		assertEquals(EMAIL, updatedPlayer.getEmail());
		assertEquals("Foo", updatedPlayer.getFirstName());
		assertEquals("Bar", updatedPlayer.getLastName());
		//second update should "fail" as version has been incremented
		assertEquals(Integer.valueOf(0), this.mapper.update(foundPlayer));
		
		allPlayers = this.mapper.getAll();
		assertNotNull(allPlayers);
		assertEquals(3,allPlayers.size());
		
		/*
		 * test delete
		 */
		this.mapper.delete(foundPlayer);
		assertNull(this.mapper.get(1));
		assertNotNull(this.mapper.get(2));
		assertNotNull(this.mapper.get(3));
	}

}
