package de.ultical.backend.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import de.ultical.backend.model.Contact;
import de.ultical.backend.model.Season;
import de.ultical.backend.model.Surface;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.TournamentFormat;
import de.ultical.backend.utils.test.PrepareDBRule;

public class TournamentEditionMapperTest {

    private static final LocalDate END_DATE = LocalDate.of(2015, 11, 1);
    private static final LocalDate START_DATE = LocalDate.of(2015, 10, 1);
    private static final String ALTERNATIVE_NAME = "mein Test";
    private static final String UPDATED_ALTERNATIVE_NAME = "different name";
    TournamentEdition edition;
    TournamentEditionMapper mapper;

    @ClassRule
    public static PrepareDBRule DBRULE = new PrepareDBRule();

    @Before
    public void setUp() throws Exception {
        this.edition = new TournamentEdition();

        TournamentFormat format = new TournamentFormat();
        format.setName("test format");
        format.setDescription("");
        DBRULE.getSession().getMapper(format.getMapper()).insert(format);
        Season season = new Season();
        season.setSurface(Surface.TURF);
        season.setYear(2015);
        DBRULE.getSession().getMapper(season.getMapper()).insert(season);

        Contact organizer = new Contact();
        organizer.setName("e.V.");
        DBRULE.getSession().getMapper(organizer.getMapper()).insert(organizer);
        this.edition.setOrganizer(organizer);

        this.edition.setTournamentFormat(format);
        this.edition.setAlternativeName(ALTERNATIVE_NAME);
        this.edition.setSeason(season);
        this.edition.setRegistrationStart(START_DATE);
        this.edition.setRegistrationEnd(END_DATE);

        this.mapper = DBRULE.getSession().getMapper(this.edition.getMapper());
        ContactMapper cm = DBRULE.getSession().getMapper(ContactMapper.class);
        cm.insert(organizer);

    }

    @Test
    public void test() {
        assertEquals(0, this.edition.getId());
        this.mapper.insert(this.edition);
        DBRULE.getSession().commit();
        final int editionId = this.edition.getId();

        TournamentEdition readEdition = this.mapper.get(editionId);
        assertNotNull(readEdition);
        assertTrue(readEdition instanceof TournamentEdition);
        assertNotNull(readEdition.getTournamentFormat());
        assertNotNull(readEdition.getSeason());
        assertNotNull(readEdition.getOrganizer());
        assertEquals(ALTERNATIVE_NAME, readEdition.getAlternativeName());
        assertEquals(1, readEdition.getVersion());

        int updateCount = this.mapper.update(this.edition);
        assertEquals(0, updateCount);

        readEdition.setAlternativeName(UPDATED_ALTERNATIVE_NAME);
        updateCount = this.mapper.update(readEdition);
        assertEquals(1, updateCount);
        readEdition = this.mapper.get(editionId);
        assertNotNull(readEdition);
        assertTrue(readEdition instanceof TournamentEdition);
        assertNotNull(readEdition.getTournamentFormat());
        assertNotNull(readEdition.getSeason());
        assertEquals(UPDATED_ALTERNATIVE_NAME, readEdition.getAlternativeName());
        assertNotNull(readEdition.getOrganizer());

        this.mapper.insert(this.edition);
        assertNotEquals(this.edition.getId(), readEdition.getId());
        List<TournamentEdition> allEditions = this.mapper.getAll();
        assertNotNull(allEditions);
        assertEquals(2, allEditions.size());

        this.mapper.delete(readEdition);
        allEditions = this.mapper.getAll();
        assertNotNull(allEditions);
        assertEquals(1, allEditions.size());

    }

}
