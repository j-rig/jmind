/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.search;

import java.sql.*;
import java.util.*;

import org.jrig.jmind.model.SearchResult;

public class NodeSearch {
    private Connection conn;
    private SearchQueryParser parser;

    public NodeSearch(Connection conn) {
        this.conn = conn;
        this.parser = new SearchQueryParser();
    }

    public void initDatabase() throws SQLException {

        // Create FTS5 virtual table for full-text search
        String createFTS = "CREATE VIRTUAL TABLE IF NOT EXISTS node_fts " +
                "USING fts5(uuid UNINDEXED, nodetext, tokenize='porter unicode61');";

        // // Create main table
        // String createMain = "
        // CREATE TABLE IF NOT EXISTS documents (
        // docid INTEGER PRIMARY KEY,
        // doctext TEXT NOT NULL
        // );
        // """;

        try (Statement stmt = conn.createStatement()) {
            // stmt.execute(createMain);
            stmt.execute(createFTS);
        }
    }

    public void updateDatabase() throws SQLException {

        // // Create FTS5 virtual table for full-text search
        // String updateFTS = "INSERT OR UPDATE INTO nodes_fts (uuid, nodetext) "+
        // "SELECT uuid, "+
        // " GROUP_CONCAT(
        // key_column || ': ' ||
        // COALESCE(value_column, '(empty)'),
        // CHAR(10)
        // ) as doctext
        // FROM (
        // SELECT uuid, type_column, key_column, value_column
        // FROM node_props
        // WHERE uuid = OLD.uuid
        // ORDER BY type_column, key_column
        // )
        // WHERE EXISTS (SELECT 1 FROM node_props WHERE uuid = OLD.uuid)
        // GROUP BY uuid;

        String updateFTS = "";

        try (Statement stmt = conn.createStatement()) {
            // stmt.execute(createMain);
            stmt.execute(updateFTS);
        }
    }

    /**
     * Search documents with ranking
     */
    public List<SearchResult> search(String query, int limit) throws SQLException {
        String ftsQuery = parser.parseQuery(query);

        String sql = "SELECT uuid, bm25(node_fts) as rank, " +
                "snippet(node_fts, 1, '<b>', '</b>', '...', 32) " +
                "as snippet FROM node_fts WHERE node_fts MATCH ? ORDER BY rank LIMIT ?";

        List<SearchResult> results = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ftsQuery);
            pstmt.setInt(2, limit);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                SearchResult result = new SearchResult(
                        rs.getString("uuid"),
                        rs.getDouble("rank"),
                        rs.getString("snippet"));
                results.add(result);
            }
        }

        return results;
    }

}