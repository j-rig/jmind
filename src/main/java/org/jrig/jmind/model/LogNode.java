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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class LogNode extends Node {

    public LogNode(Connection conn) {
        super(conn);
        try {
            this.setSysProp("type", "log");
            this.setSysPropInstant("created", Instant.now());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public LogNode(Connection conn, String uuid) {
        super(conn, uuid);
    }

    // Blog-specific methods
    public void setContent(String content) throws SQLException {
        setUserProp("content", content);
        setSysPropInstant("modified", Instant.now());
    }

    public String getContent() throws SQLException {
        return getUserProp("content", "");
    }

    public void setEntryDate(LocalDate date) throws SQLException {
        Instant instant = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        setSysPropInstant("entry_date", instant);
    }

    public LocalDate getEntryDate() throws SQLException {
        Instant instant = getSysPropInstant("entry_date", Instant.now());
        return instant.atZone(ZoneOffset.UTC).toLocalDate();
    }

    public Instant getModified() throws SQLException {
        return getSysPropInstant("modified", getSysPropInstant("created", Instant.now()));
    }

    // Static methods for querying blog entries
    private static String sqlFindByDate = "SELECT uuid FROM node_props WHERE type_column = 's' AND key_column = 'entry_date' AND value_column = ?";

    public static LogNode findByDate(Connection conn, LocalDate date) throws SQLException {
        String dateStr = instanToSqliteText(date.atStartOfDay().toInstant(ZoneOffset.UTC));
        PreparedStatement stmt = conn.prepareStatement(sqlFindByDate);
        stmt.setString(1, dateStr);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new LogNode(conn, rs.getString("uuid"));
        }
        return null;
    }

    private static String sqlFindDatesInMonth = "SELECT DISTINCT value_column FROM node_props " +
            "WHERE type_column = 's' AND key_column = 'entry_date' " +
            "AND value_column LIKE ?";

    public static List<LocalDate> findDatesWithEntries(Connection conn, int year, int month) throws SQLException {
        List<LocalDate> dates = new ArrayList<>();
        String pattern = String.format("%04d-%02d%%", year, month);

        PreparedStatement stmt = conn.prepareStatement(sqlFindDatesInMonth);
        stmt.setString(1, pattern);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            String dateStr = rs.getString("value_column");
            Instant instant = fromSqliteTextToInstant(dateStr);
            if (instant != null) {
                dates.add(instant.atZone(ZoneOffset.UTC).toLocalDate());
            }
        }
        return dates;
    }

    public static LogNode createOrGetEntry(Connection conn, LocalDate date) throws SQLException {
        LogNode existing = findByDate(conn, date);
        if (existing != null) {
            return existing;
        }

        LogNode newNode = new LogNode(conn);
        newNode.setEntryDate(date);
        return newNode;
    }
}
