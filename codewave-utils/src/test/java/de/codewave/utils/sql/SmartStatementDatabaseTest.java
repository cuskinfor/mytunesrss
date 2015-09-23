package de.codewave.utils.sql;

import de.codewave.utils.xml.*;
import junit.framework.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.utils.sql.SmartStatementDatabaseTest
 */
public class SmartStatementDatabaseTest extends TestCase {
    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        return new TestSuite(SmartStatementDatabaseTest.class);
    }

    private Connection myConnection;
    private SmartStatementFactory myFactory;

    public SmartStatementDatabaseTest(String string) {
        super(string);
    }

    @Override
    protected void setUp() throws Exception {
        myConnection = DriverManager.getConnection("jdbc:h2:mem:", "sa", "");
        Statement statement = myConnection.createStatement();
        statement.execute("create table customer (id integer, name varchar(100))");
        statement.execute("create table customer_order (id integer, customer_id integer, item_count integer, item_name varchar(100))");
        statement.execute("create table looptable (index integer, name varchar(100))");
        statement.execute("insert into customer values (1, 'Michael Descher')");
        statement.execute("insert into customer values (2, 'Tanja Glasstetter')");
        statement.execute("insert into customer values (3, 'Roland Müller')");
        statement.execute("insert into customer_order values (1, 1, 1, 'iMac 20')");
        statement.execute("insert into customer_order values (2, 1, 10, 'MS Trackball')");
        statement.execute("insert into customer_order values (3, 1, 5, 'Nokia Phone')");
        statement.execute("insert into customer_order values (4, 2, 3, 'MacBook 15')");
        statement.execute("insert into customer_order values (5, 2, 2, 'Singstar Rocks')");
        statement.execute("insert into customer_order values (6, 3, 1, 'Dell Inspiron 0815')");
        statement.execute("insert into customer_order values (7, 3, 2, 'daGama BTM Pack')");
        myFactory = SmartStatementFactory.getInstance(JXPathUtils.getContext(getClass().getResource("smart-statement-test.xml")));
    }


    @Override
    protected void tearDown() throws Exception {
        myConnection.createStatement().execute("drop all objects");
    }

    public void testFindByName() throws SQLException {
        SmartStatement statement = myFactory.createStatement(myConnection, "customerByName", Collections.<String, Boolean>emptyMap());
        statement.setString("customerName", "Michael Descher");
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            assertEquals("Michael Descher", rs.getString("name"));
            assertEquals(3, rs.getInt("orders"));
        } else {
            fail("no result set");
        }
    }

    public void testFindBigOrdersByCustomerName() throws SQLException {
        SmartStatement statement = myFactory.createStatement(myConnection, "bigOrdersByCustomerName", Collections.<String, Boolean>emptyMap());
        statement.setString("customerName", "Michael Descher");
        statement.setObject("countThreshold", new Integer(5));
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            assertEquals("Nokia Phone", rs.getString("name"));
            if (rs.next()) {
                assertEquals("MS Trackball", rs.getString("name"));
            } else {
                fail("only 1 order found");
            }
        } else {
            fail("no orders found");
        }
    }

    public void testMultiStatement() throws SQLException {
        SmartStatement statement = myFactory.createStatement(myConnection, "multiSqlSelect", Collections.<String, Boolean>emptyMap());
        statement.setString("customerName", "Max Mustermann");
        statement.setInt("customerId", 999);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            assertEquals(999, rs.getInt("id"));
        } else {
            fail("no customer found");
        }
    }

    public void testLoop() throws SQLException {
        SmartStatement statement = myFactory.createStatement(myConnection, "loopStatement", Collections.<String, Boolean>emptyMap());
        statement.setObject("name", Arrays.asList("Michael", "Roland", "Rolf", "Tanja"));
        statement.execute();
        ResultSet rs = myFactory.createStatement(myConnection, "loopQuery", Collections.<String, Boolean>emptyMap()).executeQuery();
        rs.next();
        assertEquals("Michael", rs.getString("name"));
        assertEquals(0, rs.getInt("index"));
        rs.next();
        assertEquals("Roland", rs.getString("name"));
        assertEquals(1, rs.getInt("index"));
        rs.next();
        assertEquals("Rolf", rs.getString("name"));
        assertEquals(2, rs.getInt("index"));
        rs.next();
        assertEquals("Tanja", rs.getString("name"));
        assertEquals(3, rs.getInt("index"));
    }

    public void testMapLoop() throws SQLException {
        SmartStatement statement = myFactory.createStatement(myConnection, "mapLoopStatement", Collections.<String, Boolean>emptyMap());
        Map<Integer, String> persons = new LinkedHashMap<Integer, String>();
        persons.put(100, "Mr.100");
        persons.put(101, "Mr.101");
        statement.setObject("person", persons);
        statement.execute();
        ResultSet rs = myFactory.createStatement(myConnection, "loopQuery", Collections.<String, Boolean>emptyMap()).executeQuery();
        rs.next();
        assertEquals("Mr.100", rs.getString("name"));
        assertEquals(100, rs.getInt("index"));
        rs.next();
        assertEquals("Mr.101", rs.getString("name"));
        assertEquals(101, rs.getInt("index"));
    }

    public void testDefaults() throws SQLException {
        ResultSet rs = myFactory.createStatement(myConnection, "defaultsTest", Collections.<String, Boolean>emptyMap()).executeQuery();
        rs.next();
        assertEquals("Hans", rs.getString("name"));
        assertEquals(123, rs.getInt("index"));
    }

    public void testDefaultsWithNull() throws SQLException {
        ResultSet rs = myFactory.createStatement(myConnection, "defaultsTestWithNull", Collections.<String, Boolean>emptyMap()).executeQuery();
        rs.next();
        assertNull(rs.getString("name"));
        assertEquals(123, rs.getInt("index"));
    }

  public void testArray() throws SQLException {
    SmartStatement statement = myFactory.createStatement(myConnection, "arrayTest", Collections.<String, Boolean>emptyMap());
    List<List<Object>> persons = new ArrayList<List<Object>>();
    persons.add(new ArrayList<Object>(Arrays.asList("Michael", 1)));
    persons.add(new ArrayList<Object>(Arrays.asList("Tanja", 2)));
    persons.add(new ArrayList<Object>(Arrays.asList("Roland", 3)));
    statement.setObject("person", persons);
    statement.execute();
    ResultSet rs = myFactory.createStatement(myConnection, "loopQuery", Collections.<String, Boolean>emptyMap()).executeQuery();
    rs.next();
    assertEquals("Michael", rs.getString("name"));
    assertEquals(1, rs.getInt("index"));
    rs.next();
    assertEquals("Tanja", rs.getString("name"));
    assertEquals(2, rs.getInt("index"));
    rs.next();
    assertEquals("Roland", rs.getString("name"));
    assertEquals(3, rs.getInt("index"));
  }

  public void testDynamicArray() throws SQLException {
    SmartStatement statement = myFactory.createStatement(myConnection, "dynamicTestInsert", Collections.<String, Boolean>emptyMap());
    statement.setObject("person", Arrays.asList("Michael", "Tanja", "Rolf", "Ralf", "Jürgen", "Alexander"));
    statement.execute();
    statement = myFactory.createStatement(myConnection, "dynamicTestQuery", Collections.<String, Boolean>emptyMap());
    statement.setItems("index", Arrays.asList(0, 2, 4, 5, 999));
    ResultSet rs = statement.executeQuery();
    List<String> names = new ArrayList<String>();
    while (rs.next()) {
      names.add(rs.getString("name"));
    }
    assertEquals(4, names.size());
    assertTrue(names.contains("Michael"));
    assertTrue(names.contains("Rolf"));
    assertTrue(names.contains("Jürgen"));
    assertTrue(names.contains("Alexander"));
  }
}
