package org.jrig.jmind;

import org.jrig.jmind.model.Nodes;
import org.jrig.jmind.model.Node;
// import org.jrig.jmind.layouts.MindmapLayouts;
import org.jrig.jmind.model.MindNode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import junit.framework.TestCase;

public class MindNodeTest extends TestCase {

    String DB_URL = "jdbc:sqlite::memory:";
    Connection conn;

    protected void setUp() throws Exception {
        super.setUp();
        conn = DriverManager.getConnection(DB_URL);
    }

    public void testMindNodes() throws SQLException {
        Nodes nodes = new Nodes(conn);

        MindNode rn = new MindNode(conn);
        // rn.setUserProp("r", "r");
        rn.setRoot();
        rn.setX(10);
        rn.setY(10);
        rn.setW(10);
        rn.setH(10);

        MindNode na = new MindNode(conn);
        na.setUserProp("a", "a");
        rn.addChild(na);
        na.setX(10);
        na.setY(10);
        na.setW(10);
        na.setH(10);

        MindNode nb = new MindNode(conn);
        nb.setUserProp("b", "b");
        rn.addChild(nb);
        nb.setX(10);
        nb.setY(10);
        nb.setW(10);
        nb.setH(10);

        MindNode nc = new MindNode(conn);
        nc.setUserProp("c", "c");
        na.addChild(nc);
        nc.setX(10);
        nc.setY(10);
        nc.setW(10);
        nc.setH(10);

        MindNode nd = new MindNode(conn);
        nd.setUserProp("d", "d");
        nb.addChild(nd);
        nd.setX(10);
        nd.setY(10);
        nd.setW(10);
        nd.setH(10);

        List<Node> nl = nodes.getNodes();
        assertEquals(5, nl.size());

        assertEquals(true, rn.isRoot());
        assertEquals(false, na.isRoot());

        // assertEquals(2, rn.getChildren().size());
        // assertEquals(1, na.getChildren().size());
        // assertEquals(1, nb.getChildren().size());
        // assertEquals(0, nd.getChildren().size());

        // MindmapLayouts.layoutMindmap(rn, conn);

    }

}