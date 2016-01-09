package de.ultical.backend.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.ibatis.session.SqlSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DataStoreTest {

    private DataStore ds;
    @Mock
    SqlSession sqlSession;

    @Before
    public void setUp() {
        this.ds = new DataStore();
        MockitoAnnotations.initMocks(this);
        this.ds.sqlSession = this.sqlSession;
    }

    @Test
    public void testCloseSession() {
        assertFalse(this.ds.closeSession());
        verify(this.sqlSession, never()).close();
        this.ds.setAutoCloseSession(false);
        assertTrue(this.ds.closeSession());
        verify(this.sqlSession, times(1)).close();
    }

    @Test
    public void testCloseSessionSessionNull() {
        this.ds.sqlSession = null;
        this.ds.setAutoCloseSession(false);
        assertFalse(this.ds.closeSession());
    }
}
