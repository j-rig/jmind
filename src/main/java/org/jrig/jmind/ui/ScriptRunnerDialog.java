/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.ui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.sql.Connection;
import org.python.util.PythonInterpreter;
import org.python.core.*;

public class ScriptRunnerDialog extends JDialog {
    private Connection conn;
    private String targetUuid;
    private JTextArea outputArea;
    private JButton selectScriptButton;
    private JButton runButton;
    private JButton closeButton;
    private JLabel statusLabel;
    private File selectedScript;
    private Runnable onCloseCallback;
    private JLabel scriptLabel;

    public ScriptRunnerDialog(Frame parent, Connection conn, String targetUuid, Runnable onCloseCallback) {
        super(parent, "Script Runner", true);
        this.conn = conn;
        this.targetUuid = targetUuid;
        this.onCloseCallback = onCloseCallback;

        initializeUI();
        setSize(800, 600);
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createOutputPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JLabel titleLabel = new JLabel("Jython Script Runner", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        String scopeText = (targetUuid == null)
                ? "Scope: Entire Database"
                : "Scope: Single Entry (UUID: " + targetUuid + ")";
        JLabel scopeLabel = new JLabel(scopeText);
        scopeLabel.setForeground(Color.BLUE);

        scriptLabel = new JLabel("Selected Script: None");

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.GRAY);

        infoPanel.add(scopeLabel);
        infoPanel.add(scriptLabel);
        infoPanel.add(statusLabel);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane createOutputPanel() {
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setMargin(new Insets(10, 10, 10, 10));
        outputArea.setText("Select a Python script and click 'Run Script' to execute.\n\n" +
                "The script will have access to:\n" +
                "  - conn: Database connection\n" +
                "  - uuid: Target UUID (or None for entire database)\n" +
                "  - Node, BlogNode, TreeNode classes\n");

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Output"));

        return scrollPane;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        selectScriptButton = new JButton("Select Script...");
        selectScriptButton.addActionListener(e -> selectScript());

        runButton = new JButton("Run Script");
        runButton.setEnabled(false);
        runButton.addActionListener(e -> runScript());

        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> closeDialog());

        panel.add(selectScriptButton);
        panel.add(runButton);
        panel.add(closeButton);

        return panel;
    }

    private void selectScript() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Python Script");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Python Scripts (*.py)", "py"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedScript = fileChooser.getSelectedFile();
            scriptLabel.setText("Selected Script: " + selectedScript.getName());
            runButton.setEnabled(true);
            statusLabel.setText("Script selected: " + selectedScript.getName());
            outputArea.setText("Script ready to run: " + selectedScript.getAbsolutePath() + "\n\n");
        }
    }

    private void runScript() {
        if (selectedScript == null || !selectedScript.exists()) {
            JOptionPane.showMessageDialog(this, "Please select a valid script file.");
            return;
        }

        // Disable buttons during execution
        selectScriptButton.setEnabled(false);
        runButton.setEnabled(false);
        closeButton.setEnabled(false);

        statusLabel.setText("Running script...");
        outputArea.setText("=== Script Execution Started ===\n");
        outputArea.append("Script: " + selectedScript.getName() + "\n");
        outputArea.append("Scope: " + (targetUuid == null ? "Entire Database" : "UUID: " + targetUuid) + "\n");
        outputArea.append("=================================\n\n");

        // Run script in background thread
        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return executeScript();
            }

            @Override
            protected void done() {
                try {
                    int exitCode = get();

                    outputArea.append("\n=================================\n");
                    outputArea.append("=== Script Execution Completed ===\n");
                    outputArea.append("Exit Status: " + exitCode + "\n");

                    if (exitCode == 0) {
                        statusLabel.setText("Script completed successfully");
                        statusLabel.setForeground(new Color(0, 128, 0));
                    } else {
                        statusLabel.setText("Script completed with errors");
                        statusLabel.setForeground(Color.RED);
                    }
                } catch (Exception e) {
                    outputArea.append("\n=================================\n");
                    outputArea.append("=== Script Execution Failed ===\n");
                    outputArea.append("Error: " + e.getMessage() + "\n");

                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    outputArea.append(sw.toString());

                    statusLabel.setText("Script execution failed");
                    statusLabel.setForeground(Color.RED);
                }

                // Re-enable buttons
                selectScriptButton.setEnabled(true);
                runButton.setEnabled(true);
                closeButton.setEnabled(true);
            }
        };

        worker.execute();
    }

    // Custom OutputStream that writes to the JTextArea
    private class TextAreaOutputStream extends OutputStream {
        private final JTextArea textArea;
        private final StringBuilder buffer = new StringBuilder();

        public TextAreaOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) throws IOException {
            buffer.append((char) b);
            if (b == '\n') {
                flush();
            }
        }

        @Override
        public void flush() {
            if (buffer.length() > 0) {
                final String text = buffer.toString();
                SwingUtilities.invokeLater(() -> {
                    textArea.append(text);
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                });
                buffer.setLength(0);
            }
        }
    }

    private int executeScript() {
        PythonInterpreter interpreter = null;
        try {
            // Create custom output stream that writes directly to the text area
            TextAreaOutputStream textAreaStream = new TextAreaOutputStream(outputArea);
            PrintStream printStream = new PrintStream(textAreaStream, true);

            interpreter = new PythonInterpreter();
            interpreter.setOut(printStream);
            interpreter.setErr(printStream);

            // Set up the environment
            interpreter.set("conn", conn);
            interpreter.set("uuid", targetUuid);

            // Import commonly used classes
            interpreter.exec("from org.jrig.jmind.model import Nodes, Node, LogNode, TreeNode");
            interpreter.exec("import java.sql as sql");

            // Read and execute the script
            String scriptContent = readScriptFile(selectedScript);

            // Execute the entire script
            interpreter.exec(scriptContent);

            // Ensure all output is flushed
            printStream.flush();
            textAreaStream.flush();

            return 0; // Success

        } catch (PyException e) {
            SwingUtilities.invokeLater(() -> {
                outputArea.append("\n[ERROR] Python Exception:\n");
                outputArea.append(e.toString() + "\n");

                if (e.traceback != null) {
                    outputArea.append("\nTraceback:\n");
                    StringBuilder sb = new StringBuilder();
                    e.traceback.dumpStack(sb);
                    outputArea.append(sb.toString());
                }
            });

            return 1; // Error

        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                outputArea.append("\n[ERROR] Exception:\n");
                outputArea.append(e.getMessage() + "\n");

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                outputArea.append(sw.toString());
            });

            return 1; // Error

        } finally {
            if (interpreter != null) {
                interpreter.cleanup();
                interpreter.close();
            }
        }
    }

    private String readScriptFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private void closeDialog() {
        // Call the callback to refresh the main UI
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
        dispose();
    }

    // Static factory method for easy usage
    public static void showDialog(Frame parent, Connection conn, String targetUuid, Runnable onCloseCallback) {
        ScriptRunnerDialog dialog = new ScriptRunnerDialog(parent, conn, targetUuid, onCloseCallback);
        dialog.setVisible(true);
    }
}