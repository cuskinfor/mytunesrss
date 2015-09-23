package de.codewave.utils.sql;

import de.codewave.utils.xml.*;
import junit.framework.*;

import java.util.*;

/**
 * de.codewave.utils.sql.SmartSqlTest
 */
public class SmartSqlTest extends TestCase {
    public static Test suite() {
        return new TestSuite(SmartSqlTest.class);
    }

    private SmartSql mySql;

    public SmartSqlTest(String string) {
        super(string);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mySql = new SmartSql("select * from table where col1 = :parm1 and col2 = :parm2 and col3 = :parm1", null, 0, 0);
    }

    public void testReplacements() {
        assertEquals("select * from table where col1 = ? and col2 = ? and col3 = ?", mySql.getSql());
    }

    public void testParamNames() {
        assertEquals(2, mySql.getParameterNames().size());
        assertTrue(mySql.getParameterNames().contains("parm1"));
        assertTrue(mySql.getParameterNames().contains("parm2"));
    }

    public void testParamOrder() {
        Collection<Integer> indexList = mySql.getIndexListForParameter("parm1");
        assertEquals(2, indexList.size());
        assertTrue(indexList.contains(1));
        assertTrue(indexList.contains(3));
        indexList = mySql.getIndexListForParameter("parm2");
        assertEquals(1, indexList.size());
        assertTrue(indexList.contains(2));
    }

    public void testFragments() {
        SmartStatementFactory factory = SmartStatementFactory.getInstance(JXPathUtils.getContext(getClass().getResource("smart-statement-test.xml")));
        SmartSql sql = factory.getSqls("fragmentTest").get(0);
        assertEquals("select * from table where condition", sql.getSql());
    }

    public void testArrayParameter() {
        SmartSql sql = new SmartSql("select * from table where col1 = :parm[0] and col2 = :parm[1] and col3 = :parm[2]", null, 0, 0);
        assertEquals("select * from table where col1 = ? and col2 = ? and col3 = ?", sql.getSql());
        assertTrue(sql.getIndexListForParameter("parm[0]").contains(1));
        assertTrue(sql.getIndexListForParameter("parm[1]").contains(2));
        assertTrue(sql.getIndexListForParameter("parm[2]").contains(3));
    }
}