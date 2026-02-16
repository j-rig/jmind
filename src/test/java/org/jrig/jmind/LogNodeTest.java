package org.jrig.jmind;

import org.jrig.jmind.model.Nodes;
import org.jrig.jmind.model.Node;
import org.jrig.jmind.model.LogNode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import junit.framework.TestCase;

public class LogNodeTest extends TestCase {

    String DB_URL = "jdbc:sqlite::memory:";
    Connection conn;

    protected void setUp() throws Exception {
        super.setUp();
        conn = DriverManager.getConnection(DB_URL);
    }

    public void testNodes() throws SQLException {
        Nodes nodes = new Nodes(conn);
        LogNode node = new LogNode(conn);
        node.setSysProp("test", "test");
        // System.out.println(node.getUuid());
        List<Node> nl = nodes.getNodes();
        // System.out.println(nl.size());
        assertEquals(nl.size(), 1);

    }

}