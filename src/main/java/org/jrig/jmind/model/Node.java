/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.model;

import java.util.UUID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
// import java.util.HashMap;

public class Node {

    protected String uuid;
    protected Connection conn;

    // private static HashMap<String, String> cache;

    public Node(Connection conn, String uuid) {
        this.conn = conn;
        this.uuid = uuid;
        // if (cache == null) {
        // cache = new HashMap<String, String>();
        // }

    };

    public Node(Connection conn) {
        this.conn = conn;
        this.uuid = UUID.randomUUID().toString();
        // if (cache == null) {
        // cache = new HashMap<String, String>();
        // }

    };

    public String getUuid() {
        return this.uuid;
    }

    private static String sqlRemoveNode = "DELETE FROM node_props WHERE uuid = ?";

    public static void removeNode(Connection conn, Node n) throws java.sql.SQLException {
        PreparedStatement stmt = conn.prepareStatement(sqlRemoveNode);
        stmt.setString(1, n.getUuid());
        stmt.executeUpdate();
    }

    // Core Properties
    /////////////////////////////////////////////////////////////////////////////////////////

    protected String sqlSetProp = "INSERT OR REPLACE INTO node_props (uuid, type_column, key_column, value_column) VALUES (?, ?, ?, ?)";
    protected String sqlGetProp = "SELECT value_column FROM node_props WHERE uuid  = ? AND type_column = ? AND key_column = ?";

    protected String getProp(String t, String k, String d) throws java.sql.SQLException {

        // if (cache.containsKey(uuid + t + k))
        // return cache.get(uuid + t + k);

        PreparedStatement preparedStatement = conn.prepareStatement(sqlGetProp);

        preparedStatement.setString(1, this.uuid);
        preparedStatement.setString(2, t);
        preparedStatement.setString(3, k);

        // System.out.println(preparedStatement.toString());

        ResultSet rs = preparedStatement.executeQuery();
        // System.out.println(rs.toString());

        if (rs.next()) {
            return rs.getString("value_column");

        }

        return d;

    };

    public String getSysProp(String k, String d) throws java.sql.SQLException {
        return getProp("s", k, d);
    }

    public Double getSysPropDouble(String k, Double d) throws java.sql.SQLException {
        return Double.parseDouble(getProp("s", k, Double.toString(d)));
    }

    public Instant getSysPropInstant(String k, Instant i) throws java.sql.SQLException {
        return fromSqliteTextToInstant(getProp("s", k, instanToSqliteText(i)));
    }

    public String getUserProp(String k, String d) throws java.sql.SQLException {
        return getProp("u", k, d);
    }

    protected int setProp(String t, String k, String v) throws java.sql.SQLException {

        int result = 1;

        // if (cache.containsKey(uuid + t + k) && (cache.get(uuid + t + k) == v))
        // return result;

        PreparedStatement preparedStatement = conn.prepareStatement(sqlSetProp);

        preparedStatement.setString(1, this.uuid);
        preparedStatement.setString(2, t);
        preparedStatement.setString(3, k);
        preparedStatement.setString(4, v);

        // System.out.println(preparedStatement.toString());

        result = preparedStatement.executeUpdate();
        // if (result > 0)
        // cache.put(uuid + t + k, v);
        return result;
    };

    public int setSysProp(String k, String v) throws java.sql.SQLException {
        return setProp("s", k, v);
    }

    public int setSysPropDouble(String k, double v) throws java.sql.SQLException {
        return setSysProp(k, Double.toString(v));

    }

    public int setSysPropInstant(String k, Instant i) throws java.sql.SQLException {
        return setSysProp(k, instanToSqliteText(i));
    }

    public int setUserProp(String k, String v) throws java.sql.SQLException {
        return setProp("u", k, v);
    }

    // Date and Time
    /////////////////////////////////////////////////////////////////////////////////////////

    // Formatter for YYYY-MM-DD HH:MM:SS (without milliseconds)
    private static final DateTimeFormatter SQLITE_FORMATTER_SECONDS = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter SQLITE_INPUT_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]");

    protected static String instanToSqliteText(Instant instant) {
        if (instant == null) {
            return null;
        }
        // Convert the Instant to a LocalDateTime in the UTC zone.
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        return ldt.format(SQLITE_FORMATTER_SECONDS);
    }

    protected static String dateToSqliteText(Date date) {
        if (date == null) {
            return null;
        }
        return instanToSqliteText(date.toInstant());
    }

    protected static Instant fromSqliteTextToInstant(String sqliteDateText) {
        if (sqliteDateText == null || sqliteDateText.trim().isEmpty()) {
            return null;
        }
        try {
            // Parse the string as a LocalDateTime first.
            // This treats the string as a local date-time *without* any zone offset yet.
            LocalDateTime ldt = LocalDateTime.parse(sqliteDateText, SQLITE_INPUT_FORMATTER);

            // Convert the LocalDateTime to an Instant by explicitly assigning the UTC zone
            // offset.
            // This is crucial because SQLite's text dates from DATETIME() are UTC.
            return ldt.toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            System.err.println("Failed to parse SQLite date string '" + sqliteDateText + "': " + e.getMessage());
            return null; // Or throw a custom exception, depending on error handling strategy
        }
    }

    protected static Date fromSqliteTextToDate(String sqliteDateText) {
        Instant instant = fromSqliteTextToInstant(sqliteDateText);
        return (instant != null) ? Date.from(instant) : null;
    }

}
