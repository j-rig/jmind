/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind;

import org.jrig.jmind.model.Nodes;
import org.jrig.jmind.model.TreeNode;
import org.jrig.jmind.ui.PropertyEditorPanel;
import org.jrig.jmind.ui.ScriptRunnerDialog;
import org.jrig.jmind.ui.TreePanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TreeMind extends JFrame {
    private Connection conn;
    private File currentFile;
    private TreePanel treePanel;
    private PropertyEditorPanel propertyPanel;
    private boolean isPanelsEnabled = false;

    public TreeMind() {
        setTitle("Tree Mind - jMind - No File Opened");
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

        // Left panel - Tree
        treePanel = new TreePanel();
        treePanel.setPreferredSize(new Dimension(400, 600));
        treePanel.addTreeSelectionListener(this::onNodeSelected);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Tree Structure"));
        leftPanel.add(treePanel, BorderLayout.CENTER);

        // Right panel - Properties
        propertyPanel = new PropertyEditorPanel();
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Node Properties"));
        rightPanel.add(propertyPanel, BorderLayout.CENTER);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(400);

        add(splitPane, BorderLayout.CENTER);
        setJMenuBar(createMenuBar());
    }

    private void disablePanels() {
        setComponentsEnabled(treePanel, false);
        setComponentsEnabled(propertyPanel, false);
        isPanelsEnabled = false;
        propertyPanel.setNode(null);
    }

    private void enablePanels() {
        setComponentsEnabled(treePanel, true);
        setComponentsEnabled(propertyPanel, true);
        isPanelsEnabled = true;
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

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem newFileItem = new JMenuItem("New File...");
        newFileItem.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        newFileItem.addActionListener(e -> createNewFile());

        JMenuItem openFileItem = new JMenuItem("Open File...");
        openFileItem.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        openFileItem.addActionListener(e -> selectExistingFile());

        JMenuItem closeFileItem = new JMenuItem("Close File");
        closeFileItem.addActionListener(e -> closeCurrentFile());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> exitApplication());

        fileMenu.add(newFileItem);
        fileMenu.add(openFileItem);
        fileMenu.add(closeFileItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem scriptRunnerItem = new JMenuItem("Run Script...");
        scriptRunnerItem.setAccelerator(KeyStroke.getKeyStroke("ctrl R"));
        scriptRunnerItem.addActionListener(e -> showScriptRunner());
        toolsMenu.add(scriptRunnerItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private boolean selectExistingFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Tree Database");
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
        fileChooser.setDialogTitle("Create New Tree Database");
        fileChooser.setFileFilter(new FileNameExtensionFilter("SQLite Database (*.db)", "db"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setSelectedFile(new File("my-tree.db"));

        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            if (!file.getName().toLowerCase().endsWith(".db")) {
                file = new File(file.getAbsolutePath() + ".db");
            }

            if (file.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(
                        this,
                        "File already exists. Do you want to open it instead?",
                        "File Exists",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

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
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }

            String url = "jdbc:sqlite:" + file.getAbsolutePath();
            conn = DriverManager.getConnection(url);

            // conn.createStatement().execute(
            // "CREATE TABLE IF NOT EXISTS node_props (" +
            // "uuid TEXT NOT NULL, " +
            // "type_column TEXT NOT NULL, " +
            // "key_column TEXT NOT NULL, " +
            // "value_column TEXT, " +
            // "PRIMARY KEY (uuid, type_column, key_column))"
            // );

            Nodes nodes = new Nodes(conn);

            currentFile = file;
            setTitle("Tree Mind - " + file.getName());

            enablePanels();
            treePanel.setConnection(conn);

            return true;

        } catch (SQLException e) {
            showError("Failed to open database: " + e.getMessage());
            return false;
        }
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

        disablePanels();
        setTitle("Tree Mind - jMind - No File Opened");
    }

    private void onNodeSelected(TreeNode node) {
        if (!isPanelsEnabled) {
            return;
        }
        propertyPanel.setNode(node);
    }

    private void exitApplication() {
        closeCurrentFile();
        System.exit(0);
    }

    private void showScriptRunner() {
        if (!isPanelsEnabled) {
            JOptionPane.showMessageDialog(this, "Please open a file first.");
            return;
        }

        // For tree app, pass null to run on entire database
        // Or get selected node's UUID from treePanel if you want to add that
        // functionality
        ScriptRunnerDialog.showDialog(this, conn, null, () -> {
            // Refresh UI after script execution
            treePanel.refreshTree();
        });
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(
                this,
                "Tree Mind - jMind\n" +
                        "A hierarchical tree node editor\n" +
                        "© 2026 j-rig",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            TreeMind app = new TreeMind();
            app.setVisible(true);
        });
    }
}
