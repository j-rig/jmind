package org.jrig.jmind;

import org.jrig.jmind.model.Nodes;
import org.jrig.jmind.model.Node;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import junit.framework.TestCase;

public class NodeTest extends TestCase {

    String DB_URL = "jdbc:sqlite::memory:";
    Connection conn;

    protected void setUp() throws Exception {
        super.setUp();
        conn = DriverManager.getConnection(DB_URL);
    }

    public void testNodes() throws SQLException {
        Nodes nodes = new Nodes(conn);
        Node node = new Node(conn);
        assertEquals(1, node.setSysProp("test1", "test1"));
        assertEquals(1, node.setSysProp("test2", "test2"));
        assertEquals(1, node.setSysPropDouble("test3", 1.0));
        Instant ts = Instant.now();
        assertEquals(1, node.setSysPropInstant("test4", ts));
        assertEquals(1, node.setUserProp("test5", "test5"));

        assertEquals("test1", node.getSysProp("test1", "default"));
        assertEquals("test2", node.getSysProp("test2", "default"));
        assertEquals(1.0, node.getSysPropDouble("test3", 0.0));
        // assertEquals(ts, node.getSysPropInstant("test4", Instant.MIN)); //TODO
        assertEquals("test5", node.getUserProp("test5", "default"));

        List<Node> nl = nodes.getNodes();
        assertEquals(nl.size(), 1);

    }

}