package de.ultical.backend.data;

import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.apache.ibatis.session.SqlSession;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.data.mapper.DfvMvNameMapper;
import de.ultical.backend.data.mapper.DfvPlayerMapper;
import de.ultical.backend.model.DfvPlayer;

public class DataStoreTest {

    private DataStore dStore;
    @Mock
    SqlSession sqlSession;
    @Mock
    DfvPlayerMapper playerMapper;
    @Mock
    DfvMvNameMapper nameMapper;
    final int dfvNrOne = 123;
    final int dfvNrTwo = 234;
    final int dfvNrThree = 345;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(this.sqlSession.getMapper(DfvPlayerMapper.class)).thenReturn(this.playerMapper);
        when(this.sqlSession.getMapper(DfvMvNameMapper.class)).thenReturn(this.nameMapper);

        when(this.playerMapper.getAll()).thenReturn(Arrays.asList(this.buildPlayer("Foo", "Bar", this.dfvNrOne, true),
                this.buildPlayer("Foo", "Bar", this.dfvNrTwo, true),
                this.buildPlayer("Foo", "Bar", this.dfvNrThree, true)));
        when(this.nameMapper.get(this.dfvNrOne)).thenReturn(this.buildName("Foo", "Bar", this.dfvNrOne, true));
        when(this.nameMapper.get(this.dfvNrTwo)).thenReturn(this.buildName("Foo", "Bàr", this.dfvNrTwo, true));
        when(this.nameMapper.get(this.dfvNrThree)).thenReturn(this.buildName("Foo", "Bar", this.dfvNrThree, false));
        this.dStore = new DataStore();
        this.dStore.sqlSession = this.sqlSession;
    }

    private DfvPlayer buildPlayer(String fn, String ln, int dfvNr, boolean eligible) {
        DfvPlayer player = new DfvPlayer();
        player.setFirstName(fn);
        player.setLastName(ln);
        player.setDfvNumber(dfvNr);
        if (eligible) {
            player.setEligibleUntil(null);
        } else {
            player.setEligibleUntil(LocalDateTime.now().minusMonths(1));
        }
        return player;
    }

    private DfvMvName buildName(String fn, String ln, int dfvNr, boolean active) {
        DfvMvName result = new DfvMvName();
        result.setFirstName(fn);
        result.setLastName(ln);
        result.setDfvnr(dfvNr);
        result.setActive(active);
        return result;
    }

}
