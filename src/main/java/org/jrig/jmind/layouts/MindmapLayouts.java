/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.layouts;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jrig.jmind.model.MindNode;
import org.jrig.jmind.model.Node;

public class MindmapLayouts {

    public static MindNode layoutRight(MindNode root) {
        return new TidyTreeLayout(true).layout(root);
    }

    public static MindNode layoutLeft(MindNode root) {
        MindNode result = new TidyTreeLayout(true).layout(root);
        result.mirrorHorizontal();
        return result;
    }

    public static MindNode layoutDown(MindNode root) {
        return new TidyTreeLayout(false).layout(root);
    }

    public static MindNode layoutUp(MindNode root) {
        MindNode result = new TidyTreeLayout(false).layout(root);
        result.mirrorVertical();
        return result;
    }

    public static MindNode layoutMindmap(MindNode root, Connection conn) {
        int n = root.getChildren().size();
        int rightCount = (n / 2) + (n % 2);

        // Create temporary roots for each side
        List<MindNode> leftChildren = new ArrayList<>();
        List<MindNode> rightChildren = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (i < rightCount) {
                rightChildren.add(root.getChildren().get(i));
            } else {
                leftChildren.add(root.getChildren().get(i));
            }
        }

        MindNode leftRoot = new MindNode(conn);
        leftRoot.setW(root.getW());
        leftRoot.setH(root.getH());
        leftRoot.setChildren(leftChildren);

        MindNode rightRoot = new MindNode(conn);
        rightRoot.setW(root.getW());
        rightRoot.setH(root.getH());
        rightRoot.setChildren(rightChildren);

        // Layout both sides
        layoutRight(rightRoot);
        layoutLeft(leftRoot);

        // Align both sides
        rightRoot.translate(leftRoot.getX() - rightRoot.getX(),
                leftRoot.getY() - rightRoot.getY());

        // Position original root
        root.setX(leftRoot.getX());
        root.setY(rightRoot.getY());

        // get ride of temp roots
        try {
            Node.removeNode(conn, rightRoot);
            Node.removeNode(conn, leftRoot);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // todo handle unlinked nodes

        return root;
    }
}