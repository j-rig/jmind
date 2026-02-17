/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.ui;

import org.jrig.jmind.model.TreeNode;
import org.jrig.jmind.model.Node;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.*;
// import java.awt.dnd.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TreePanel extends JPanel {
    private JTree tree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private Connection conn;
    private List<TreeSelectionListener> listeners;
    private TreeNode rootTreeNode;
    
    public interface TreeSelectionListener {
        void nodeSelected(TreeNode node);
    }
    
    public TreePanel() {
        this.listeners = new ArrayList<>();
        setLayout(new BorderLayout());
        
        rootNode = new DefaultMutableTreeNode("Root");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        
        // Enable drag and drop
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.setTransferHandler(new TreeTransferHandler());
        
        // Selection listener
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode != null && selectedNode.getUserObject() instanceof TreeNodeWrapper) {
                TreeNodeWrapper wrapper = (TreeNodeWrapper) selectedNode.getUserObject();
                notifyListeners(wrapper.getTreeNode());
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane, BorderLayout.CENTER);
        add(createToolbar(), BorderLayout.NORTH);
    }
    
    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        JButton addChildButton = new JButton("Add Child");
        addChildButton.addActionListener(e -> addChildNode());
        
        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(e -> removeNode());
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshTree());
        
        toolbar.add(addChildButton);
        toolbar.add(removeButton);
        toolbar.addSeparator();
        toolbar.add(refreshButton);
        
        return toolbar;
    }
    
    public void setConnection(Connection conn) {
        this.conn = conn;
        try {
            // Get or create root node
            rootTreeNode = TreeNode.getOrCreateRoot(conn);
            refreshTree();
            
            // Auto-select root node
            tree.setSelectionRow(0);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error initializing root: " + e.getMessage());
        }
    }
    
    public void refreshTree() {
        if (conn == null || rootTreeNode == null) {
            return;
        }
        
        try {
            // Create root node wrapper
            TreeNodeWrapper rootWrapper = new TreeNodeWrapper(rootTreeNode);
            rootNode.setUserObject(rootWrapper);
            rootNode.removeAllChildren();
            
            // Load children
            loadChildren(rootNode, rootTreeNode);
            
            treeModel.reload();
            expandAll();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading tree: " + e.getMessage());
        }
    }
    
    private void loadChildren(DefaultMutableTreeNode parent, TreeNode parentNode) throws SQLException {
        List<TreeNode> children = parentNode.getChildren();
        for (TreeNode child : children) {
            TreeNodeWrapper wrapper = new TreeNodeWrapper(child);
            DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(wrapper);
            parent.add(childTreeNode);
            loadChildren(childTreeNode, child);
        }
    }
    
    private void expandAll() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }
    
    private void addChildNode() {
        if (conn == null) {
            return;
        }
        
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (selectedNode == null || !(selectedNode.getUserObject() instanceof TreeNodeWrapper)) {
            JOptionPane.showMessageDialog(this, "Please select a parent node first.");
            return;
        }
        
        TreeNodeWrapper parentWrapper = (TreeNodeWrapper) selectedNode.getUserObject();
        String name = JOptionPane.showInputDialog(this, "Enter node name:", "New Child Node", JOptionPane.PLAIN_MESSAGE);
        
        if (name != null && !name.trim().isEmpty()) {
            try {
                TreeNode node = new TreeNode(conn);
                node.setName(name.trim());
                node.setParentUuid(parentWrapper.getTreeNode().getUuid());
                refreshTree();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error creating node: " + e.getMessage());
            }
        }
    }
    
    private void removeNode() {
        if (conn == null) {
            return;
        }
        
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (selectedNode == null || !(selectedNode.getUserObject() instanceof TreeNodeWrapper)) {
            JOptionPane.showMessageDialog(this, "Please select a node to remove.");
            return;
        }
        
        TreeNodeWrapper wrapper = (TreeNodeWrapper) selectedNode.getUserObject();
        
        // Prevent removal of root node
        if (wrapper.getTreeNode().isRoot()) {
            JOptionPane.showMessageDialog(
                this,
                "The root node cannot be removed.",
                "Cannot Remove Root",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        try {
            if (wrapper.getTreeNode().hasChildren()) {
                int result = JOptionPane.showConfirmDialog(
                    this,
                    "This node has children. Remove all children too?",
                    "Confirm Remove",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (result == JOptionPane.CANCEL_OPTION) {
                    return;
                } else if (result == JOptionPane.YES_OPTION) {
                    removeNodeRecursive(wrapper.getTreeNode());
                } else {
                    return;
                }
            } else {
                int result = JOptionPane.showConfirmDialog(
                    this,
                    "Remove node '" + wrapper.getTreeNode().getName() + "'?",
                    "Confirm Remove",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (result == JOptionPane.YES_OPTION) {
                    Node.removeNode(conn, wrapper.getTreeNode());
                }
            }
            refreshTree();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error removing node: " + e.getMessage());
        }
    }
    
    private void removeNodeRecursive(TreeNode node) throws SQLException {
        // Don't remove root
        if (node.isRoot()) {
            return;
        }
        
        List<TreeNode> children = node.getChildren();
        for (TreeNode child : children) {
            removeNodeRecursive(child);
        }
        Node.removeNode(conn, node);
    }
    
    public void addTreeSelectionListener(TreeSelectionListener listener) {
        listeners.add(listener);
    }
    
    private void notifyListeners(TreeNode node) {
        for (TreeSelectionListener listener : listeners) {
            listener.nodeSelected(node);
        }
    }
    
    // Wrapper class for tree nodes
    private static class TreeNodeWrapper {
        private TreeNode treeNode;
        
        public TreeNodeWrapper(TreeNode treeNode) {
            this.treeNode = treeNode;
        }
        
        public TreeNode getTreeNode() {
            return treeNode;
        }
        
        @Override
        public String toString() {
            try {
                return treeNode.getName();
            } catch (SQLException e) {
                return "Error";
            }
        }
    }
    
    // Transfer handler for drag and drop
    private class TreeTransferHandler extends TransferHandler {
        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }
        
        @Override
        protected Transferable createTransferable(JComponent c) {
            JTree tree = (JTree) c;
            TreePath path = tree.getSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.getUserObject() instanceof TreeNodeWrapper) {
                    TreeNodeWrapper wrapper = (TreeNodeWrapper) node.getUserObject();
                    // Don't allow dragging root
                    if (wrapper.getTreeNode().isRoot()) {
                        return null;
                    }
                    return new StringSelection(wrapper.getTreeNode().getUuid());
                }
            }
            return null;
        }
        
        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop()) {
                return false;
            }
            
            JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
            TreePath path = dropLocation.getPath();
            
            return path != null;
        }
        
        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            
            try {
                String draggedUuid = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
                TreePath path = dropLocation.getPath();
                
                DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                String newParentUuid = "";
                
                if (targetNode.getUserObject() instanceof TreeNodeWrapper) {
                    TreeNodeWrapper wrapper = (TreeNodeWrapper) targetNode.getUserObject();
                    newParentUuid = wrapper.getTreeNode().getUuid();
                    
                    // Check if trying to move to itself or its descendant
                    if (draggedUuid.equals(newParentUuid) || isDescendant(draggedUuid, newParentUuid)) {
                        JOptionPane.showMessageDialog(
                            TreePanel.this,
                            "Cannot move a node to itself or its descendant.",
                            "Invalid Move",
                            JOptionPane.WARNING_MESSAGE
                        );
                        return false;
                    }
                }
                
                TreeNode draggedNode = new TreeNode(conn, draggedUuid);
                draggedNode.setParentUuid(newParentUuid);
                
                refreshTree();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        
        private boolean isDescendant(String ancestorUuid, String nodeUuid) throws SQLException {
            TreeNode node = new TreeNode(conn, nodeUuid);
            while (node.hasParent()) {
                String parentUuid = node.getParentUuid();
                if (parentUuid.equals(ancestorUuid)) {
                    return true;
                }
                node = new TreeNode(conn, parentUuid);
            }
            return false;
        }
    }
}