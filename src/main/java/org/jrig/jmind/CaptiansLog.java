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
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CaptiansLog extends JFrame {
    private Connection conn;
    private File currentFile;
    private MonthCalendarPanel calendarPanel;
    private TextEditorPanel editorPanel;
    private LogNode currentLog;
    private LocalDate currentDate;
    
    private JButton saveButton;
    private JButton clearButton;
    private boolean hasUnsavedChanges = false;
    private boolean isPanelsEnabled = false;
    private Timer changeDetectionTimer;
    
    public CaptiansLog() {
        this.currentDate = LocalDate.now();
        
        setTitle("Captians Log - jMind - No File Opened");
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
        disablePanels();
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
        
        // Change detection timer
        changeDetectionTimer = new Timer(1000, e -> checkForChanges());
    }
    
    private void disablePanels() {
        // Disable all interactive components
        setComponentsEnabled(calendarPanel, false);
        setComponentsEnabled(editorPanel, false);
        saveButton.setEnabled(false);
        clearButton.setEnabled(false);
        isPanelsEnabled = false;
        
        // Stop change detection
        if (changeDetectionTimer != null) {
            changeDetectionTimer.stop();
        }
        
        // Show placeholder message
        editorPanel.setText("No file opened.\n\nUse File > New File or File > Open File to get started.");
        editorPanel.setStatusText("No file opened");
        
        // Visual indication
        calendarPanel.setBackground(new Color(240, 240, 240));
    }
    
    private void enablePanels() {
        // Enable all interactive components
        setComponentsEnabled(calendarPanel, true);
        setComponentsEnabled(editorPanel, true);
        clearButton.setEnabled(true);
        isPanelsEnabled = true;
        
        // Start change detection
        if (changeDetectionTimer != null) {
            changeDetectionTimer.start();
        }
        
        // Restore visual state
        calendarPanel.setBackground(null);
    }
    
    private void setComponentsEnabled(Container container, boolean enabled) {
        container.setEnabled(enabled);
        for (Component component : container.getComponents()) {
            component.setEnabled(enabled);
            if (component instanceof Container) {
                setComponentsEnabled((Container) component, enabled);
            }
        }
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveCurrentLog());
        saveButton.setEnabled(false);
        
        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearLog());
        clearButton.setEnabled(false);
        
        panel.add(clearButton);
        panel.add(saveButton);
        
        return panel;
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem newFileItem = new JMenuItem("New File...");
        newFileItem.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        newFileItem.addActionListener(e -> {
            if (confirmCloseCurrentFile()) {
                createNewFile();
            }
        });
        
        JMenuItem openFileItem = new JMenuItem("Open File...");
        openFileItem.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        openFileItem.addActionListener(e -> {
            if (confirmCloseCurrentFile()) {
                selectExistingFile();
            }
        });
        
        JMenuItem closeFileItem = new JMenuItem("Close File");
        closeFileItem.addActionListener(e -> {
            if (confirmCloseCurrentFile()) {
                closeCurrentFile();
            }
        });
        
        JMenuItem saveItem = new JMenuItem("Save Log");
        saveItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        saveItem.addActionListener(e -> saveCurrentLog());
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> exitApplication());
        
        fileMenu.add(newFileItem);
        fileMenu.add(openFileItem);
        fileMenu.add(closeFileItem);
        fileMenu.addSeparator();
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
    
    private boolean selectExistingFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Captians Log Database");
        fileChooser.setFileFilter(new FileNameExtensionFilter("SQLite Database (*.db)", "db"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.exists()) {
                showError("File does not exist: " + file.getName());
                return false;
            }
            return openDatabase(file);
        }
        return false;
    }
    
    private boolean createNewFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Create New Captians Log Database");
        fileChooser.setFileFilter(new FileNameExtensionFilter("SQLite Database (*.db)", "db"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setSelectedFile(new File("my-logs.db"));
        
        int result = fileChooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            // Ensure .db extension
            if (!file.getName().toLowerCase().endsWith(".db")) {
                file = new File(file.getAbsolutePath() + ".db");
            }
            
            // Check if file already exists
            if (file.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(
                    this,
                    "File already exists. Do you want to open it instead?",
                    "File Exists",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (overwrite != JOptionPane.YES_OPTION) {
                    return false;
                }
            }
            
            return openDatabase(file);
        }
        return false;
    }
    
    private boolean openDatabase(File file) {
        try {
            // Close existing connection if any
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
            
            // Open new connection
            String url = "jdbc:sqlite:" + file.getAbsolutePath();
            conn = DriverManager.getConnection(url);
            
            // // Create table if not exists
            // conn.createStatement().execute(
            //     "CREATE TABLE IF NOT EXISTS node_props (" +
            //     "uuid TEXT NOT NULL, " +
            //     "type_column TEXT NOT NULL, " +
            //     "key_column TEXT NOT NULL, " +
            //     "value_column TEXT, " +
            //     "PRIMARY KEY (uuid, type_column, key_column))"
            // );

            Nodes nodes= new Nodes(conn);
            
            currentFile = file;
            setTitle("Captians Log - " + file.getName());
            
            // Enable panels and load data
            enablePanels();
            loadLog(currentDate);
            loadEntriesForCurrentMonth();
            
            editorPanel.setStatusText("Opened: " + file.getName());
            
            return true;
            
        } catch (SQLException e) {
            showError("Failed to open database: " + e.getMessage());
            return false;
        }
    }
    
    private boolean confirmCloseCurrentFile() {
        if (!isPanelsEnabled) {
            return true;
        }
        
        if (hasUnsavedChanges) {
            int result = JOptionPane.showConfirmDialog(
                this,
                "You have unsaved changes. Save before closing?",
                "Unsaved Changes",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result == JOptionPane.CANCEL_OPTION) {
                return false;
            } else if (result == JOptionPane.YES_OPTION) {
                saveCurrentLog();
            }
        }
        return true;
    }
    
    private void closeCurrentFile() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        conn = null;
        currentFile = null;
        currentLog = null;
        hasUnsavedChanges = false;
        
        disablePanels();
        setTitle("Captians Log - jMind - No File Opened");
    }
    
    private void onDateSelected(LocalDate date) {
        if (!isPanelsEnabled) {
            return;
        }
        
        if (!date.equals(currentDate)) {
            // Check for unsaved changes
            if (hasUnsavedChanges) {
                int result = JOptionPane.showConfirmDialog(
                    this,
                    "You have unsaved changes. Save before switching?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (result == JOptionPane.CANCEL_OPTION) {
                    return;
                } else if (result == JOptionPane.YES_OPTION) {
                    saveCurrentLog();
                }
            }
            
            currentDate = date;
            loadLog(date);
        }
    }
    
    private void loadLog(LocalDate date) {
        if (!isPanelsEnabled) {
            return;
        }
        
        try {
            currentLog = LogNode.findByDate(conn, date);
            
            if (currentLog != null) {
                String content = currentLog.getContent();
                editorPanel.setText(content);
                editorPanel.setStatusText("Loaded log for " + formatDate(date));
            } else {
                editorPanel.clear();
                editorPanel.setStatusText("New log for " + formatDate(date));
            }
            
            setTitle("Captians Log - " + currentFile.getName() + " - " + formatDate(date));
            hasUnsavedChanges = false;
            saveButton.setEnabled(false);
            editorPanel.requestEditorFocus();
            
        } catch (SQLException e) {
            showError("Error loading log: " + e.getMessage());
        }
    }
    
    private void saveCurrentLog() {
        if (!isPanelsEnabled) {
            return;
        }
        
        try {
            String content = editorPanel.getText();
            
            if (currentLog == null) {
                currentLog = LogNode.createOrGetEntry(conn, currentDate);
            }
            
            currentLog.setContent(content);
            
            String timeStr = java.time.LocalTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss")
            );
            editorPanel.setStatusText("Saved at " + timeStr);
            
            hasUnsavedChanges = false;
            saveButton.setEnabled(false);
            
            loadEntriesForCurrentMonth();
            
        } catch (SQLException e) {
            showError("Error saving log: " + e.getMessage());
        }
    }
    
    private void clearLog() {
        if (!isPanelsEnabled) {
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "Clear all content for this log? This will remove all text.",
            "Confirm Clear",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            editorPanel.clear();
            hasUnsavedChanges = true;
            saveButton.setEnabled(true);
            editorPanel.setStatusText("Log cleared (not saved)");
        }
    }
    
    private void checkForChanges() {
        if (!isPanelsEnabled) {
            return;
        }
        
        try {
            if (currentLog != null) {
                String currentContent = editorPanel.getText();
                String savedContent = currentLog.getContent();
                boolean changed = !currentContent.equals(savedContent);
                
                if (changed != hasUnsavedChanges) {
                    hasUnsavedChanges = changed;
                    saveButton.setEnabled(changed);
                    
                    if (changed) {
                        editorPanel.setStatusText("Unsaved changes");
                    }
                }
            } else {
                // New log - any content is a change
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
        } catch (SQLException e) {
            // Ignore
        }
    }
    
    private void goToToday() {
        if (!isPanelsEnabled) {
            return;
        }
        
        currentDate = LocalDate.now();
        loadLog(currentDate);
        loadEntriesForCurrentMonth();
    }
    
    private void loadEntriesForCurrentMonth() {
        if (!isPanelsEnabled) {
            return;
        }
        
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
        if (confirmCloseCurrentFile()) {
            closeCurrentFile();
            System.exit(0);
        }
    }
    
    private void showAbout() {
        JOptionPane.showMessageDialog(
            this,
            "Captians Log - jMind\n" +
            "A simple daily logging application\n" +
            "© 2026 j-rig",
            "About",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        if (isPanelsEnabled) {
            editorPanel.setStatusText("Error: " + message);
        }
    }
    
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create and show application
        SwingUtilities.invokeLater(() -> {
            CaptiansLog app = new CaptiansLog();
            app.setVisible(true);
        });
    }
}
