/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Nodes {

    private Connection conn;

    private static String createTableSQL = "CREATE TABLE IF NOT EXISTS node_props (" +
            "uuid TEXT PRIMARY KEY, " +
            "type_column TEXT NOT NULL, " +
            "key_column TEXT NOT NULL, " +
            "value_column TEXT" +
            ")";

    private String sqlGetAllUuids = "SELECT uuid FROM node_props";

    public static int createNodesTable(Connection conn) throws java.sql.SQLException {
        PreparedStatement stmt = conn.prepareStatement(createTableSQL);
        return stmt.executeUpdate();
    }

    public Nodes(Connection conn) throws java.sql.SQLException {
        this.conn = conn;
        // create table if not exists
        PreparedStatement stmt = conn.prepareStatement(createTableSQL);
        stmt.executeUpdate();
    };

    public Node getNode(String uuid) {
        return new Node(this.conn, uuid);
    }

    public void removeNode(Node n) throws java.sql.SQLException {
        Node.removeNode(n.conn, n);
    }

    public List<String> getAllUuids() throws java.sql.SQLException {
        List<String> result = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement(sqlGetAllUuids);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            result.add(rs.getString("uuid"));
        }
        return result;
    }

    public List<Node> getNodes() throws java.sql.SQLException {
        List<Node> result = new ArrayList<>();
        for (String uuid : getAllUuids()) {
            result.add(new Node(this.conn, uuid));
        }
        return result;
    }

}
