/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.ui;

import org.jrig.jmind.model.TreeNode;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

public class PropertyEditorPanel extends JPanel {
    private TreeNode currentNode;
    private JLabel uuidLabel;
    private JLabel nodeTypeLabel;
    private JTable sysPropsTable;
    private JTable userPropsTable;
    private DefaultTableModel sysPropsModel;
    private DefaultTableModel userPropsModel;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;

    public PropertyEditorPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createTablesPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // JLabel titleLabel = new JLabel("Properties", SwingConstants.CENTER);
        // titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        uuidLabel = new JLabel("No node selected", SwingConstants.CENTER);
        uuidLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        uuidLabel.setForeground(Color.GRAY);

        nodeTypeLabel = new JLabel("", SwingConstants.CENTER);
        nodeTypeLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        nodeTypeLabel.setForeground(Color.BLUE);

        JPanel labelPanel = new JPanel(new GridLayout(2, 1));
        labelPanel.add(uuidLabel);
        labelPanel.add(nodeTypeLabel);

        // panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(labelPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTablesPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // System properties (read-only)
        sysPropsModel = new DefaultTableModel(new String[] { "Property", "Value" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        sysPropsTable = new JTable(sysPropsModel);
        sysPropsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        sysPropsTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        sysPropsTable.getColumnModel().getColumn(1).setPreferredWidth(300);

        JScrollPane sysPropsScroll = new JScrollPane(sysPropsTable);
        tabbedPane.addTab("System Properties", sysPropsScroll);

        // User properties (editable)
        JPanel userPropsPanel = new JPanel(new BorderLayout());

        userPropsModel = new DefaultTableModel(new String[] { "Property", "Value" }, 0);
        userPropsTable = new JTable(userPropsModel);
        userPropsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        userPropsTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        userPropsTable.getColumnModel().getColumn(1).setPreferredWidth(300);

        JScrollPane userPropsScroll = new JScrollPane(userPropsTable);
        userPropsPanel.add(userPropsScroll, BorderLayout.CENTER);
        userPropsPanel.add(createUserPropsToolbar(), BorderLayout.SOUTH);

        tabbedPane.addTab("User Properties", userPropsPanel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createUserPropsToolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        addButton = new JButton("Add");
        addButton.addActionListener(e -> addUserProperty());

        updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateUserProperty());

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteUserProperty());

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);

        return panel;
    }

    public void setNode(TreeNode node) {
        this.currentNode = node;
        if (node == null) {
            clear();
            return;
        }

        try {
            uuidLabel.setText("UUID: " + node.getUuid());

            if (node.isRoot()) {
                nodeTypeLabel.setText("ROOT NODE (Cannot be removed)");
            } else {
                nodeTypeLabel.setText("");
            }

            loadSystemProperties();
            loadUserProperties();

            // User properties are always editable, even for root
            addButton.setEnabled(true);
            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading properties: " + e.getMessage());
        }
    }

    private void loadSystemProperties() throws SQLException {
        sysPropsModel.setRowCount(0);

        if (currentNode == null) {
            return;
        }

        Map<String, String> props = new TreeMap<>(currentNode.getSystemProperties());
        for (Map.Entry<String, String> entry : props.entrySet()) {
            sysPropsModel.addRow(new Object[] { entry.getKey(), entry.getValue() });
        }
    }

    private void loadUserProperties() throws SQLException {
        userPropsModel.setRowCount(0);

        if (currentNode == null) {
            return;
        }

        Map<String, String> props = new TreeMap<>(currentNode.getUserProperties());
        for (Map.Entry<String, String> entry : props.entrySet()) {
            userPropsModel.addRow(new Object[] { entry.getKey(), entry.getValue() });
        }
    }

    private void addUserProperty() {
        if (currentNode == null) {
            JOptionPane.showMessageDialog(this, "No node selected.");
            return;
        }

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField keyField = new JTextField();
        JTextField valueField = new JTextField();

        panel.add(new JLabel("Property Name:"));
        panel.add(keyField);
        panel.add(new JLabel("Property Value:"));
        panel.add(valueField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add User Property",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String key = keyField.getText().trim();
            String value = valueField.getText().trim();

            if (key.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Property name cannot be empty.");
                return;
            }

            try {
                currentNode.setUserProp(key, value);
                loadUserProperties();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error adding property: " + e.getMessage());
            }
        }
    }

    private void updateUserProperty() {
        if (currentNode == null) {
            JOptionPane.showMessageDialog(this, "No node selected.");
            return;
        }

        int selectedRow = userPropsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a property to update.");
            return;
        }

        String key = (String) userPropsModel.getValueAt(selectedRow, 0);
        String currentValue = (String) userPropsModel.getValueAt(selectedRow, 1);

        String newValue = JOptionPane.showInputDialog(
                this,
                "Update value for '" + key + "':",
                currentValue);

        if (newValue != null) {
            try {
                currentNode.setUserProp(key, newValue);
                loadUserProperties();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error updating property: " + e.getMessage());
            }
        }
    }

    private void deleteUserProperty() {
        if (currentNode == null) {
            JOptionPane.showMessageDialog(this, "No node selected.");
            return;
        }

        int selectedRow = userPropsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a property to delete.");
            return;
        }

        String key = (String) userPropsModel.getValueAt(selectedRow, 0);

        int result = JOptionPane.showConfirmDialog(
                this,
                "Delete property '" + key + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            try {
                currentNode.deleteUserProp(key);
                loadUserProperties();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting property: " + e.getMessage());
            }
        }
    }

    private void clear() {
        uuidLabel.setText("No node selected");
        nodeTypeLabel.setText("");
        sysPropsModel.setRowCount(0);
        userPropsModel.setRowCount(0);
        addButton.setEnabled(false);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
}