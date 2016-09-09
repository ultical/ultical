package de.ultical.backend.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.exceptions.PersistenceException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

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
        this.dfvMvName = new DfvMvName();
        this.dfvMvName.setDfvnr(3234567);
        this.dfvMvName.setVorname(VORNAME);
        this.dfvMvName.setNachname(NACHNAME);
        this.dfvMvName.setDse(true);
        this.dfvMvName.setLastModified(LocalDateTime.now());
        this.mapper = DBRULE.getSession().getMapper(DfvMvNameMapper.class);
    }

    @Test
    public void test() {
        Integer id = this.mapper.insert(this.dfvMvName);
        assertNotNull(id);
        DfvMvName found = this.mapper.get(3234567);
        assertNotNull(found);
        assertEquals(VORNAME, found.getFirstName());
        assertEquals(NACHNAME, found.getLastName());
        assertEquals(true, found.isDse());

        DfvMvName other = new DfvMvName();
        other.setDfvnr(41234568);
        other.setFirstName(VORNAME);
        other.setLastName(NACHNAME);
        other.setLastModified(LocalDateTime.now());
        this.mapper.insert(other);

        other = new DfvMvName();
        other.setDfvnr(51234569);
        other.setVorname(VORNAME);
        other.setNachname(NACHNAME);
        other.setLastModified(LocalDateTime.now());
        this.mapper.insert(other);

        List<DfvMvName> names = this.mapper.getAll();
        assertNotNull(names);
        assertEquals(3, names.size());

        this.mapper.deleteAll();
        assertNull(this.mapper.get(32234567));
        assertNull(this.mapper.get(41234568));
        assertNull(this.mapper.get(51234569));
    }

    @Test(expected = PersistenceException.class)
    public void testPrimaryKeyConstraintViolation() throws Exception {
        try {
            this.mapper.insert(this.dfvMvName);
            this.mapper.insert(this.dfvMvName);
        } finally {
            // avoids an exception in case this test is run before the test()
            // method.
            this.mapper.deleteAll();
        }
    }

}
