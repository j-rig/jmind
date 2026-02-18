/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.ui;

import org.jrig.jmind.io.CsvIo;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.util.function.Supplier;

public class CsvIoDialog {

    /**
     * Show export dialog and export data to CSV
     * 
     * @param parent Parent frame for dialog positioning
     * @param conn   Database connection
     * @return true if export was successful, false otherwise
     */
    public static boolean showExportDialog(Frame parent, Connection conn) {
        if (conn == null) {
            JOptionPane.showMessageDialog(parent, "No database connection available.");
            return false;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export to CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setSelectedFile(new File("export.csv"));

        int result = fileChooser.showSaveDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Ensure .csv extension
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }

            // Check if file exists
            if (file.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(
                        parent,
                        "File already exists. Overwrite?",
                        "File Exists",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (overwrite != JOptionPane.YES_OPTION) {
                    return false;
                }
            }

            try {
                CsvIo.saveNodes(file, conn);
                JOptionPane.showMessageDialog(
                        parent,
                        "Data exported successfully to:\n" + file.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                return true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        parent,
                        "Export failed: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    /**
     * Show import dialog and import data from CSV
     * 
     * @param parent    Parent frame for dialog positioning
     * @param conn      Database connection
     * @param onSuccess Callback to run after successful import (for UI refresh)
     * @return true if import was successful, false otherwise
     */
    public static boolean showImportDialog(Frame parent, Connection conn, Runnable onSuccess) {
        if (conn == null) {
            JOptionPane.showMessageDialog(parent, "No database connection available.");
            return false;
        }

        int confirm = JOptionPane.showConfirmDialog(
                parent,
                "Importing will add data to the current database.\nContinue?",
                "Confirm Import",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return false;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import from CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        int result = fileChooser.showOpenDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            if (!file.exists()) {
                JOptionPane.showMessageDialog(
                        parent,
                        "File does not exist: " + file.getName(),
                        "File Not Found",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            try {
                CsvIo.loadNodes(file, conn);

                // Call success callback to refresh UI
                if (onSuccess != null) {
                    onSuccess.run();
                }

                JOptionPane.showMessageDialog(
                        parent,
                        "Data imported successfully from:\n" + file.getAbsolutePath(),
                        "Import Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                return true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        parent,
                        "Import failed: " + e.getMessage(),
                        "Import Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    /**
     * Add CSV import/export menu items to a menu
     * Uses a Supplier to get the connection dynamically at runtime
     * 
     * @param menu            Menu to add items to
     * @param parent          Parent frame
     * @param connSupplier    Supplier that provides the current database connection
     * @param onImportSuccess Callback after successful import
     */
    public static void addCsvMenuItems(JMenu menu, Frame parent, Supplier<Connection> connSupplier,
            Runnable onImportSuccess) {
        JMenuItem exportCsvItem = new JMenuItem("Export to CSV...");
        exportCsvItem.setAccelerator(KeyStroke.getKeyStroke("ctrl E"));
        exportCsvItem.addActionListener(e -> {
            Connection conn = connSupplier.get();
            showExportDialog(parent, conn);
        });

        JMenuItem importCsvItem = new JMenuItem("Import from CSV...");
        importCsvItem.setAccelerator(KeyStroke.getKeyStroke("ctrl I"));
        importCsvItem.addActionListener(e -> {
            Connection conn = connSupplier.get();
            showImportDialog(parent, conn, onImportSuccess);
        });

        menu.add(exportCsvItem);
        menu.add(importCsvItem);
    }
}