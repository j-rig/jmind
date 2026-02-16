/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind;

import org.jrig.jmind.model.LogNode;
import org.jrig.jmind.model.Nodes;
import org.jrig.jmind.ui.MonthCalendarPanel;
import org.jrig.jmind.ui.TextEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CaptiansLog extends JFrame {
    private Connection conn;
    private MonthCalendarPanel calendarPanel;
    private TextEditorPanel editorPanel;
    private LogNode currentNote;
    private LocalDate currentDate;

    private JButton saveButton;
    private boolean hasUnsavedChanges = false;

    public CaptiansLog(Connection conn) {
        this.conn = conn;
        this.currentDate = LocalDate.now();

        setTitle("Captians Log - jMind");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exitApplication();
            }
        });
        setSize(1200, 700);
        setLocationRelativeTo(null);

        initializeUI();
        loadNote(currentDate);
        loadEntriesForCurrentMonth();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));

        // Left panel - Calendar
        calendarPanel = new MonthCalendarPanel();
        calendarPanel.setPreferredSize(new Dimension(350, 600));
        calendarPanel.addDateSelectionListener(this::onDateSelected);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Calendar"));
        leftPanel.add(calendarPanel, BorderLayout.CENTER);

        // Right panel - Editor
        editorPanel = new TextEditorPanel();
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Daily Log"));
        rightPanel.add(editorPanel, BorderLayout.CENTER);
        rightPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        // Add panels to frame
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // Menu bar
        setJMenuBar(createMenuBar());

        // Track changes
        editorPanel.getText(); // Initialize
        new Timer(1000, e -> checkForChanges()).start();
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveCurrentNote());
        saveButton.setEnabled(false);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearNote());

        panel.add(clearButton);
        panel.add(saveButton);

        return panel;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        saveItem.addActionListener(e -> saveCurrentNote());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> exitApplication());

        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu viewMenu = new JMenu("View");
        JMenuItem todayItem = new JMenuItem("Go to Today");
        todayItem.setAccelerator(KeyStroke.getKeyStroke("ctrl T"));
        todayItem.addActionListener(e -> goToToday());
        viewMenu.add(todayItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private void onDateSelected(LocalDate date) {
        if (!date.equals(currentDate)) {
            // Check for unsaved changes
            if (hasUnsavedChanges) {
                int result = JOptionPane.showConfirmDialog(
                        this,
                        "You have unsaved changes. Save before switching?",
                        "Unsaved Changes",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (result == JOptionPane.CANCEL_OPTION) {
                    return; // Don't switch dates
                } else if (result == JOptionPane.YES_OPTION) {
                    saveCurrentNote();
                }
            }

            currentDate = date;
            loadNote(date);
        }
    }

    private void loadNote(LocalDate date) {
        try {
            currentNote = LogNode.findByDate(conn, date);

            if (currentNote != null) {
                String content = currentNote.getContent();
                editorPanel.setText(content);
                editorPanel.setStatusText("Loaded log for " + formatDate(date));
            } else {
                editorPanel.clear();
                editorPanel.setStatusText("New log for " + formatDate(date));
            }

            setTitle("Captians Log - " + formatDate(date));
            hasUnsavedChanges = false;
            saveButton.setEnabled(false);
            editorPanel.requestEditorFocus();

        } catch (SQLException e) {
            showError("Error loading log: " + e.getMessage());
        }
    }

    private void saveCurrentNote() {
        try {
            String content = editorPanel.getText();

            // Always allow saving, even empty content (to clear a note)
            if (currentNote == null) {
                currentNote = LogNode.createOrGetEntry(conn, currentDate);
            }

            currentNote.setContent(content);

            String timeStr = java.time.LocalTime.now().format(
                    DateTimeFormatter.ofPattern("HH:mm:ss"));
            editorPanel.setStatusText("Saved at " + timeStr);

            hasUnsavedChanges = false;
            saveButton.setEnabled(false);

            // Refresh calendar to show this date has an entry (if content is not empty)
            loadEntriesForCurrentMonth();

        } catch (SQLException e) {
            showError("Error saving log: " + e.getMessage());
        }
    }

    private void clearNote() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Clear all content for this log? This will remove all text and formatting.",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            editorPanel.clear();
            hasUnsavedChanges = true;
            saveButton.setEnabled(true);
            editorPanel.setStatusText("Log cleared (not saved)");
        }
    }

    private void checkForChanges() {
        // Simple change detection - could be enhanced
        if (currentNote != null) {
            try {
                String currentContent = editorPanel.getText();
                String savedContent = currentNote.getContent();
                boolean changed = !currentContent.equals(savedContent);

                if (changed != hasUnsavedChanges) {
                    hasUnsavedChanges = changed;
                    saveButton.setEnabled(changed);

                    if (changed) {
                        editorPanel.setStatusText("Unsaved changes");
                    }
                }
            } catch (SQLException e) {
                // Ignore
            }
        } else {
            // New note - any content is a change
            String currentContent = editorPanel.getText();
            boolean changed = !currentContent.trim().isEmpty();

            if (changed != hasUnsavedChanges) {
                hasUnsavedChanges = changed;
                saveButton.setEnabled(changed);

                if (changed) {
                    editorPanel.setStatusText("Unsaved changes");
                }
            }
        }
    }

    private void goToToday() {
        currentDate = LocalDate.now();

        // Update calendar to current month if needed
        YearMonth currentYearMonth = YearMonth.from(currentDate);
        if (!currentYearMonth.equals(calendarPanel.getCurrentMonth())) {
            // Calendar will be updated when loadNote is called
        }

        loadNote(currentDate);
        loadEntriesForCurrentMonth();
    }

    private void loadEntriesForCurrentMonth() {
        try {
            int year = calendarPanel.getCurrentMonth().getYear();
            int month = calendarPanel.getCurrentMonth().getMonthValue();

            List<LocalDate> dates = LogNode.findDatesWithEntries(conn, year, month);
            Set<LocalDate> dateSet = new HashSet<>(dates);

            calendarPanel.setDatesWithEntries(dateSet);

        } catch (SQLException e) {
            showError("Error loading entry dates: " + e.getMessage());
        }
    }

    private void exitApplication() {
        if (hasUnsavedChanges) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "You have unsaved changes. Save before exiting?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.CANCEL_OPTION) {
                return; // Don't exit
            } else if (result == JOptionPane.YES_OPTION) {
                saveCurrentNote();
            }
        }

        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(
                this,
                "Captains Log - jMind\n" +
                        "Version 1.0\n\n" +
                        "A simple daily note-taking application\n" +
                        "© 2026 j-rig",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        editorPanel.setStatusText("Error: " + message);
    }

    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Database connection
        SwingUtilities.invokeLater(() -> {
            try {
                Connection conn = DriverManager.getConnection("jdbc:sqlite:jmind.db");

                // // Create table if not exists
                // conn.createStatement().execute(
                // "CREATE TABLE IF NOT EXISTS node_props (" +
                // "uuid TEXT NOT NULL, " +
                // "type_column TEXT NOT NULL, " +
                // "key_column TEXT NOT NULL, " +
                // "value_column TEXT, " +
                // "PRIMARY KEY (uuid, type_column, key_column))"
                // );

                Nodes nodes = new Nodes(conn);

                CaptiansLog app = new CaptiansLog(conn);
                app.setVisible(true);

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to connect to database: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}