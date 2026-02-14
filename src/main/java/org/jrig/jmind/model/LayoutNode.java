/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.model;

import java.util.ArrayList;
import java.util.List;

public class LayoutNode {
    public double w; // width
    public double h; // height
    public double y; // position
    public List<LayoutNode> children;

    // Layout properties
    public double prelim = 0.0;
    public double mod = 0.0;
    public double shift = 0.0;
    public double change = 0.0;

    // Extreme nodes
    public LayoutNode extremeLeft = null;
    public LayoutNode extremeRight = null;
    public double modLeft = 0.0;
    public double modRight = 0.0;

    // Threading
    public LayoutNode threadLeft = null;
    public LayoutNode threadRight = null;

    public LayoutNode(double w, double h, double y, List<LayoutNode> children) {
        this.w = w;
        this.h = h;
        this.y = y;
        this.children = children != null ? children : new ArrayList<>();
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public double bottom() {
        return y + h;
    }

    public static LayoutNode fromNode(MindNode node, boolean horizontal) {
        List<LayoutNode> children = new ArrayList<>();
        for (MindNode child : node.getChildren()) {
            children.add(fromNode(child, horizontal));
        }

        if (horizontal) {
            return new LayoutNode(node.getH(), node.getW(), node.getY(), children);
        } else {
            return new LayoutNode(node.getW(), node.getH(), node.getX(), children);
        }
    }
}