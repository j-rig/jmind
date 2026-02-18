/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.io;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.jrig.jmind.model.Node;
import org.jrig.jmind.model.Nodes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CsvIo {

    private static String sqlSave = "SELECT * from node_props";

    public static void saveNodes(File file, Connection conn) throws IOException, java.sql.SQLException {

        String[] headers = { "uuid", "type_column", "key_column", "value_column" };

        CSVFormat format = CSVFormat.EXCEL.builder()
                .setHeader(headers)
                .get();

        FileWriter out = new FileWriter(file);
        CSVPrinter printer = new CSVPrinter(out, format);

        PreparedStatement stmt = conn.prepareStatement(sqlSave);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            printer.printRecord(
                    rs.getString("uuid"),
                    rs.getString("type_column"),
                    rs.getString("key_column"),
                    rs.getString("value_column"));
        }

        printer.close();
        out.close();

    }

    public static void loadNodes(File file, Connection conn) throws IOException, java.sql.SQLException {
        Reader in = new FileReader(file);
        Iterable<CSVRecord> records = CSVFormat.EXCEL.builder()
                .setHeader() // Indicates that the first record contains headers
                .setSkipHeaderRecord(true) // Skips the header record during iteration
                .get()
                .parse(in);
        Nodes.createNodesTable(conn);
        for (CSVRecord record : records) {
            Node n = new Node(conn, record.get("uuid"));
            if (record.get("type_column").equals("s")) {
                n.setSysProp(record.get("key_column"), record.get("value_column"));
            } else {
                n.setUserProp(record.get("key_column"), record.get("value_column"));
            }
        }

    }

}
