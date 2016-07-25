package de.ultical.backend.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import de.ultical.backend.model.Contact;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.Gender;
import de.ultical.backend.model.Season;
import de.ultical.backend.model.Surface;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.TournamentFormat;
import de.ultical.backend.model.User;
import de.ultical.backend.utils.test.PrepareDBRule;

public class TournamentFormatTest {

    private static final String DESCRIPTION = "bestes Strandturnier ever";
    private static final String URL = "goldstrand.de";
    private static final String NAME = "Goldstrand";

    @ClassRule
    public static PrepareDBRule DBRULE = new PrepareDBRule();

    private TournamentFormat format;
    private TournamentFormatMapper mapper;

    private User admin;

    @Before
    public void setUp() throws Exception {
        this.format = new TournamentFormat();
        this.format.setName(NAME);
        this.format.setDescription(DESCRIPTION);
        this.format.setUrl(URL);

        final DfvPlayer player = new DfvPlayer();
        player.setFirstName("Brodie");
        player.setLastName("Smith");
        player.setGender(Gender.MALE);
        player.setDfvNumber(123456);
        player.setBirthDate(LocalDate.of(1979, 1, 25));
        player.setLastModified(LocalDateTime.now());
        DBRULE.getSession().getMapper(PlayerMapper.class).insertPlayer(player, player instanceof DfvPlayer);
        DBRULE.getSession().getMapper(player.getMapper()).insert(player);

        this.admin = new User();
        this.admin.setDfvPlayer(player);
        this.admin.setEmail("q@q.de");
        this.admin.setPassword("secret");
        DBRULE.getSession().getMapper(this.admin.getMapper()).insert(this.admin);

        this.mapper = DBRULE.getSession().getMapper(this.format.getMapper());
    }

    @Test
    public void test() {
        assertEquals(0, this.format.getId());
        this.mapper.insert(this.format);
        DBRULE.getSession().commit();
        int formatId = this.format.getId();

        TournamentFormat readFormat = this.mapper.get(formatId);
        assertNotNull(readFormat);
        assertEquals(1, readFormat.getVersion());
        assertEquals(NAME, readFormat.getName());
        assertEquals(DESCRIPTION, readFormat.getDescription());
        assertEquals(URL, readFormat.getUrl());

        this.format.setName("SOTB");
        int updCount = this.mapper.update(this.format);
        assertEquals(0, updCount);

        readFormat.setName("SOTB");
        updCount = this.mapper.update(readFormat);
        assertEquals(1, updCount);
        DBRULE.getSession().commit();

        readFormat = this.mapper.get(formatId);
        assertEquals("SOTB", readFormat.getName());
        assertEquals(DESCRIPTION, readFormat.getDescription());
        assertEquals(URL, readFormat.getUrl());
        assertEquals(2, readFormat.getVersion());

        this.mapper.insert(this.format);
        DBRULE.getSession().commit();

        List<TournamentFormat> allFormats = this.mapper.getAll();
        assertNotNull(allFormats);
        assertEquals(2, allFormats.size());

        this.mapper.insertAdmin(this.format, this.admin);
        readFormat = this.mapper.get(this.format.getId());
        assertNotNull(readFormat);
        assertNotNull(readFormat.getAdmins());
        assertFalse(readFormat.getAdmins().isEmpty());
        assertEquals(1, readFormat.getAdmins().size());

        /*
         * create a sample TournamentEdtion to check whether they were set
         * correctly.
         */
        TournamentEdition tes = new TournamentEdition();
        Season season = new Season();
        season.setYear(2015);
        season.setSurface(Surface.TURF);
        tes.setSeason(season);
        tes.setTournamentFormat(this.format);
        tes.setRegistrationStart(LocalDate.of(2015, 10, 1));
        tes.setRegistrationEnd(LocalDate.of(2015, 12, 21));

        Contact organizer = new Contact();
        organizer.setEmail("orga@nizer.de");
        tes.setOrganizer(organizer);

        /*
         * after these three lines, the TournamentFormat format should have two
         * editions.
         */
        DBRULE.getSession().getMapper(ContactMapper.class).insert(organizer);
        DBRULE.getSession().getMapper(SeasonMapper.class).insert(season);
        DBRULE.getSession().getMapper(TournamentEditionMapper.class).insert(tes);
        DBRULE.getSession().getMapper(TournamentEditionMapper.class).insert(tes);

        TournamentFormat formatWithEditions = this.mapper.get(this.format.getId());
        assertNotNull(formatWithEditions);
        assertNotNull(formatWithEditions.getEditions());
        assertEquals(2, formatWithEditions.getEditions().size());

        this.mapper.delete(this.format);
        DBRULE.getSession().commit();

        allFormats = this.mapper.getAll();
        assertNotNull(allFormats);
        assertEquals(1, allFormats.size());
        assertNull(this.mapper.get(2));

    }

}
