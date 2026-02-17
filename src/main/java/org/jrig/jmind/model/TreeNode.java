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
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
// import java.util.HashMap;
import java.util.List;
// import java.util.Map;

public class TreeNode extends Node {

    public static final String ROOT_UUID = "ROOT";

    public TreeNode(Connection conn) {
        super(conn);
        try {
            this.setSysProp("type", "tree");
            // this.setSysPropInstant("created", Instant.now());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public TreeNode(Connection conn, String uuid) {
        super(conn, uuid);
    }

    // Tree-specific methods
    public void setName(String name) throws SQLException {
        setSysProp("name", name);
        setSysPropInstant("modified", Instant.now());
    }

    public String getName() throws SQLException {
        return getSysProp("name", "Untitled");
    }

    public void setParentUuid(String parentUuid) throws SQLException {
        if (parentUuid == null || parentUuid.isEmpty()) {
            setSysProp("parent", "");
        } else {
            setSysProp("parent", parentUuid);
        }
        setSysPropInstant("modified", Instant.now());
    }

    public String getParentUuid() throws SQLException {
        return getSysProp("parent", "");
    }

    public boolean hasParent() throws SQLException {
        String parent = getParentUuid();
        return parent != null && !parent.isEmpty();
    }

    public boolean isRoot() {
        return ROOT_UUID.equals(this.uuid);
    }

    // public Instant getCreated() throws SQLException {
    // return getSysPropInstant("created", Instant.now());
    // }

    // public Instant getModified() throws SQLException {
    // return getSysPropInstant("modified", getSysPropInstant("created",
    // Instant.now()));
    // }

    // // Get all user properties
    // private static String sqlGetUserProps =
    // "SELECT key_column, value_column FROM node_props WHERE uuid = ? AND
    // type_column = 'u'";

    // public Map<String, String> getUserProperties() throws SQLException {
    // Map<String, String> props = new HashMap<>();
    // PreparedStatement stmt = conn.prepareStatement(sqlGetUserProps);
    // stmt.setString(1, this.uuid);
    // ResultSet rs = stmt.executeQuery();

    // while (rs.next()) {
    // props.put(rs.getString("key_column"), rs.getString("value_column"));
    // }
    // return props;
    // }

    // // Get all system properties
    // private static String sqlGetSysProps =
    // "SELECT key_column, value_column FROM node_props WHERE uuid = ? AND
    // type_column = 's'";

    // public Map<String, String> getSystemProperties() throws SQLException {
    // Map<String, String> props = new HashMap<>();
    // PreparedStatement stmt = conn.prepareStatement(sqlGetSysProps);
    // stmt.setString(1, this.uuid);
    // ResultSet rs = stmt.executeQuery();

    // while (rs.next()) {
    // props.put(rs.getString("key_column"), rs.getString("value_column"));
    // }
    // return props;
    // }

    // // Delete a user property
    // private static String sqlDeleteUserProp =
    // "DELETE FROM node_props WHERE uuid = ? AND type_column = 'u' AND key_column =
    // ?";

    // public void deleteUserProp(String key) throws SQLException {
    // PreparedStatement stmt = conn.prepareStatement(sqlDeleteUserProp);
    // stmt.setString(1, this.uuid);
    // stmt.setString(2, key);
    // stmt.executeUpdate();
    // }

    // Static methods for querying tree nodes
    private static String sqlCheckRootExists = "SELECT COUNT(*) as count FROM node_props WHERE uuid = ? AND type_column = 's' AND key_column = 'type'";

    public static TreeNode getOrCreateRoot(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sqlCheckRootExists);
        stmt.setString(1, ROOT_UUID);
        ResultSet rs = stmt.executeQuery();

        if (rs.next() && rs.getInt("count") > 0) {
            return new TreeNode(conn, ROOT_UUID);
        }

        // Create root node
        TreeNode root = new TreeNode(conn, ROOT_UUID);
        root.setSysProp("type", "tree");
        root.setName("Root");
        root.setSysPropInstant("created", Instant.now());
        root.setParentUuid("");

        return root;
    }

    private static String sqlFindChildren = "SELECT uuid FROM node_props WHERE type_column = 's' AND key_column = 'parent' AND value_column = ?";

    public static List<TreeNode> findChildren(Connection conn, String parentUuid) throws SQLException {
        List<TreeNode> nodes = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement(sqlFindChildren);
        stmt.setString(1, parentUuid);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            nodes.add(new TreeNode(conn, rs.getString("uuid")));
        }
        return nodes;
    }

    private static String sqlFindAllTreeNodes = "SELECT DISTINCT uuid FROM node_props WHERE type_column = 's' AND key_column = 'type' AND value_column = 'tree'";

    public static List<TreeNode> findAllTreeNodes(Connection conn) throws SQLException {
        List<TreeNode> nodes = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement(sqlFindAllTreeNodes);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            nodes.add(new TreeNode(conn, rs.getString("uuid")));
        }
        return nodes;
    }

    // Check if this node has children
    public boolean hasChildren() throws SQLException {
        return !findChildren(conn, this.uuid).isEmpty();
    }

    // Get children
    public List<TreeNode> getChildren() throws SQLException {
        return findChildren(conn, this.uuid);
    }
}
