/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.layouts;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jrig.jmind.model.LayoutNode;
import org.jrig.jmind.model.MindNode;

public class TidyTreeLayout {
    private final boolean horizontal;

    public TidyTreeLayout(boolean horizontal) {
        this.horizontal = horizontal;
    }

    public MindNode layout(MindNode root) {
        // Step 1: Initialize layer positions [1]
        layer(root, 0);

        // Step 2: Convert to layout tree [1]
        LayoutNode layoutTree = LayoutNode.fromNode(root, horizontal);

        // Step 3: Run tidy tree algorithm [1]
        firstWalk(layoutTree);
        secondWalk(layoutTree, 0);

        // Step 4: Convert back to original tree [1]
        applyPositions(layoutTree, root);

        // Step 5: Normalize to remove negative coordinates [1]
        normalize(root);

        return root;
    }

    private void layer(MindNode node, double depth) {
        if (horizontal) {
            node.setX(depth);
            depth += node.getW();
        } else {
            node.setY(depth);
            depth += node.getH();
        }

        for (MindNode child : node.getChildren()) {
            layer(child, depth);
        }
    }

    private void firstWalk(LayoutNode tree) {
        if (tree.isLeaf()) {
            setExtremes(tree);
            return;
        }

        // Layout first child [1]
        firstWalk(tree.children.get(0));
        List<Double> contour = new ArrayList<>();
        contour.add(tree.children.get(0).bottom());

        // Layout remaining children with separation [1]
        for (int i = 1; i < tree.children.size(); i++) {
            firstWalk(tree.children.get(i));
            separate(tree, i, contour);
            contour.add(tree.children.get(i).bottom());
        }

        // Center parent over children [1]
        positionRoot(tree);
        setExtremes(tree);
    }

    private void separate(LayoutNode tree, int idx, List<Double> contour) {
        LayoutNode leftSibling = tree.children.get(idx - 1);
        LayoutNode current = tree.children.get(idx);

        double modLeft = leftSibling.mod;
        double modCurrent = current.mod;

        while (leftSibling != null && current != null) {
            if (leftSibling.bottom() > contour.get(0)) {
                contour.remove(0);
            }

            // Calculate required separation [1]
            double distance = modLeft + leftSibling.prelim + leftSibling.w -
                    (modCurrent + current.prelim);

            if (distance > 0) {
                modCurrent += distance;
                moveSubtree(tree, idx, distance);
            }

            double leftY = leftSibling.bottom();
            double currentY = current.bottom();

            if (leftY <= currentY) {
                leftSibling = nextRight(leftSibling);
                if (leftSibling != null) {
                    modLeft += leftSibling.mod;
                }
            }

            if (leftY >= currentY) {
                current = nextLeft(current);
                if (current != null) {
                    modCurrent += current.mod;
                }
            }
        }
    }

    private void moveSubtree(LayoutNode tree, int idx, double distance) {
        tree.children.get(idx).mod += distance;
        tree.children.get(idx).modLeft += distance;
        tree.children.get(idx).modRight += distance;

        if (idx > 1) {
            double ratio = distance / idx;
            tree.children.get(idx).shift += distance;
            tree.children.get(idx).change -= ratio;
            tree.children.get(0).change += ratio;
        }
    }

    private void positionRoot(LayoutNode tree) {
        LayoutNode left = tree.children.get(0);
        LayoutNode right = tree.children.get(tree.children.size() - 1);
        tree.prelim = (left.prelim + left.mod +
                right.prelim + right.mod + right.w) / 2 - tree.w / 2;
    }

    private void setExtremes(LayoutNode tree) {
        if (tree.isLeaf()) {
            tree.extremeLeft = tree.extremeRight = tree;
            tree.modLeft = tree.modRight = 0;
        } else {
            tree.extremeLeft = tree.children.get(0).extremeLeft;
            tree.modLeft = tree.children.get(0).modLeft;
            tree.extremeRight = tree.children.get(tree.children.size() - 1).extremeRight;
            tree.modRight = tree.children.get(tree.children.size() - 1).modRight;
        }
    }

    private void secondWalk(LayoutNode tree, double modSum) {
        modSum += tree.mod;
        tree.y = tree.prelim + modSum;
        addChildSpacing(tree);

        for (LayoutNode child : tree.children) {
            secondWalk(child, modSum);
        }
    }

    private void addChildSpacing(LayoutNode tree) {
        double delta = 0.0;
        double modDelta = 0.0;

        for (LayoutNode child : tree.children) {
            delta += child.shift;
            modDelta += delta + child.change;
            child.mod += modDelta;
        }
    }

    private LayoutNode nextLeft(LayoutNode tree) {
        return tree.isLeaf() ? tree.threadLeft : tree.children.get(0);
    }

    private LayoutNode nextRight(LayoutNode tree) {
        return tree.isLeaf() ? tree.threadRight : tree.children.get(tree.children.size() - 1);
    }

    private void applyPositions(LayoutNode layout, MindNode node) {
        if (horizontal) {
            node.setY(layout.y);
        } else {
            node.setX(layout.y);
        }

        for (int i = 0; i < node.getChildren().size(); i++) {
            if (i < layout.children.size()) {
                applyPositions(layout.children.get(i), node.getChildren().get(i));
            }
        }
    }

    private void normalize(MindNode node) {
        // String coord = horizontal ? "y" : "x";
        List<MindNode> nodes = new ArrayList<>();
        try {
            node.walk(nodes::add);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        double minVal = nodes.stream()
                .mapToDouble(n -> horizontal ? n.getY() : n.getX())
                .min()
                .orElse(0.0);

        if (minVal < 0) {
            if (horizontal) {
                node.translate(0, -minVal);
            } else {
                node.translate(-minVal, 0);
            }
        }
    }
}
