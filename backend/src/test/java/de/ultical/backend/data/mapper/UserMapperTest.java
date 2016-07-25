package de.ultical.backend.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.Gender;
import de.ultical.backend.model.User;
import de.ultical.backend.utils.test.PrepareDBRule;

public class UserMapperTest {

    private static final String UPDATED_EMAIL = "new@ultical.com";
    private static final String EMAIL = "test@ultical.com";
    private static final String PASSWORD = "secret";
    private static DfvPlayer player;
    private SqlSession session;
    private User user;

    @ClassRule
    public static PrepareDBRule DBRULE = new PrepareDBRule();

    @BeforeClass
    public static void beforeClass() throws Exception {
        SqlSession staticSession = DBRULE.getSession();
        player = new DfvPlayer();
        player.setFirstName("Brodie");
        player.setLastName("Smith");
        player.setGender(Gender.MALE);
        player.setDfvNumber(123456);
        player.setBirthDate(LocalDate.of(1979, 1, 25));
        player.setLastModified(LocalDateTime.now());

        DfvPlayerMapper playerMapper = staticSession.getMapper(DfvPlayerMapper.class);
        PlayerMapper pMapper = staticSession.getMapper(PlayerMapper.class);
        pMapper.insertPlayer(player, player instanceof DfvPlayer);
        playerMapper.insert(player);
        staticSession.commit();
        DBRULE.closeSession();

    }

    @Before
    public void setUp() throws Exception {
        this.session = DBRULE.getSession();
        this.user = new User();
        this.user.setEmail(EMAIL);
        this.user.setPassword(PASSWORD);
        this.user.setEmailConfirmed(true);
        this.user.setDfvEmailOptIn(true);

        this.user.setDfvPlayer(player);
        this.session.commit();
    }

    @Test
    public void test() {
        UserMapper userMapper = this.session.getMapper(UserMapper.class);
        userMapper.insert(this.user);
        this.session.commit();

        User foundUser = userMapper.getByEmail(EMAIL);
        assertNotNull(foundUser);
        assertEquals(PASSWORD, foundUser.getPassword());
        assertEquals(1, foundUser.getId());
        assertEquals(1, foundUser.getVersion());
        assertEquals(true, foundUser.isEmailConfirmed());
        assertEquals(true, foundUser.isDfvEmailOptIn());
        assertNotNull(foundUser.getDfvPlayer());

        /*
         * test getAll
         */
        List<User> allUsers = userMapper.getAll();
        assertNotNull(allUsers);
        assertEquals(1, allUsers.size());
        // inserting the user once again, and check if getAll still works.
        userMapper.insert(this.user);
        allUsers = userMapper.getAll();
        assertNotNull(allUsers);
        assertEquals(2, allUsers.size());

        /*
         * test update of user
         */
        foundUser.setEmail(UPDATED_EMAIL);
        final int updateCount = userMapper.update(foundUser);
        assertEquals(1, updateCount);
        foundUser = userMapper.get(1);
        assertNotNull(foundUser);
        assertEquals(UPDATED_EMAIL, foundUser.getEmail());
        assertEquals(2, foundUser.getVersion());

        /*
         * test delete
         */
        userMapper.delete(foundUser);
        assertNull(userMapper.get(1));
    }

    @Test(expected = PersistenceException.class)
    public void violateForeignKey() {
        UserMapper userMapper = this.session.getMapper(UserMapper.class);
        DfvPlayer nonExistingPlayer = new DfvPlayer();
        nonExistingPlayer.setId(1024);
        this.user.setDfvPlayer(nonExistingPlayer);
        userMapper.insert(this.user);
    }
}
