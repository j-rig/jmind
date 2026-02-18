/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

public class DotIo {

    public static List<String> getValidNodeAttributes() {
        return Arrays.asList(
                // --- Core Content & Identification ---
                "label", // The text string displayed on the node.
                         // Can be plain text, HTML-like labels, or an empty string.

                // --- Visual Appearance & Shape ---
                "shape", // The shape of the node. Common values:
                         // "box", "ellipse", "circle", "diamond", "triangle",
                         // "plaintext", "record", "Mrecord", "point", "egg",
                         // "star", "note", "tab", "folder", "component", etc.
                         // See Graphviz docs for a full list.

                "style", // Visual style of the node. Can be a comma-separated list.
                         // Common values: "filled", "rounded", "dashed", "dotted",
                         // "bold", "invisible", "solid", "doubleoctagon", etc.

                "color", // The color of the node's border.
                "fillcolor", // The fill color of the node (if style="filled" is set).
                "fontcolor", // The color of the label text.

                "fontname", // The font family used for the label text (e.g., "Helvetica", "Arial",
                            // "Times-Roman").
                "fontsize", // The font size in points for the label text.
                "margin", // Margin around the label, in inches. E.g., "0.1,0.05".
                "peripheries", // Number of concentric peripheries (outlines) for the node. Default is 1.

                // --- Sizing & Dimensions ---
                "width", // Minimum width of the node in inches.
                "height", // Minimum height of the node in inches.
                "fixedsize", // If "true", node size is fixed by width/height. If "false" (default),
                             // it grows to fit the label. Can also be "shape" to fix to shape boundary.

                // --- Image & Icon ---
                "image", // Path to an image file to display inside the node.
                "imagescale", // How to scale the image. "true", "false", "width", "height", "both".
                "imagepos", // Position of the image relative to the label ("tl", "tc", "tr", "ml", "mc",
                            // "mr", "bl", "bc", "br").

                // --- Interactivity (for SVG, HTML outputs) ---
                "tooltip", // Text to display when hovering over the node.
                "URL", // A URL to link to when the node is clicked.
                "target", // Target frame for the URL (e.g., "_blank").

                // --- Layout & Grouping Hints ---
                "group", // Nodes sharing the same group name are constrained to be on the same rank.
                "rank", // (More common as subgraph attribute or in rank constraint)
                        // "same", "min", "max", "source", "sink".
                "pos", // Position of the node, usually computed by layout engine. Can be set manually.
                "pin", // If "true", the node's position is fixed and won't be moved by the layout.

                // --- Less Common but Valid ---
                "comment", // Any string. Ignored by Graphviz, but can be used for metadata.
                "id", // An external ID, useful for external tools.
                "labeljust", // Justification for multi-line labels ("l", "c", "r").
                "labelloc", // Vertical placement of label ("t", "c", "b").
                "perifillcolor", // Fill color for peripheries (if style=filled and peripheries > 1).
                "peripheriescolor", // Color for peripheries.
                "peripheriesstyle", // Style for peripheries.
                "peripheriestype" // Type of peripheries.
        );
    }

    private static String sqlTreeSave = "SELECT distinct uuid from node_props order by uuid";

    private static String sqlTreeFindChildren = "SELECT * FROM node_props WHERE type_column = 's' AND key_column = 'parent' order by uuid";

    public static void saveTreeNodes(File file, Connection conn) throws IOException, java.sql.SQLException {

        List<String> nodeAttr = getValidNodeAttributes();

        FileWriter out = new FileWriter(file);

        out.write("digraph JMindGraph {\n\n");

        // node defs
        PreparedStatement stmt = conn.prepareStatement(sqlTreeSave);
        ResultSet rs = stmt.executeQuery();

        // TODO build properties
        while (rs.next()) {
            out.write("\"" + rs.getString("uuid") + "\";\n");
        }

        out.write("\n\n");

        // edge defs
        stmt = conn.prepareStatement(sqlTreeFindChildren);
        rs = stmt.executeQuery();
        while (rs.next()) {
            out.write("\"" + rs.getString("uuid") + "\" -> \"" + rs.getString("value_column") + "\";\n");

        }

        out.write("\n\n}\n");

        out.close();

    }
}
