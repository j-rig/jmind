/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import java.awt.Rectangle;

public class MindNode extends Node {

    public MindNode(Connection conn) {
        super(conn);
    }

    public MindNode(Connection conn, String uuid) {
        super(conn, uuid);
    }

    private static final String CHILD_K = "nodeChildren";

    private String[] getChildrenUuids() throws java.sql.SQLException {
        String scuuids = getSysProp(CHILD_K, "");
        String[] cuuids = scuuids.split(",");
        return cuuids;
    }

    public List<MindNode> getChildren() {
        List<MindNode> result = new ArrayList<>();
        String[] cuuids;
        try {
            cuuids = getChildrenUuids();

            for (String cuid : cuuids) {
                result.add(new MindNode(this.conn, cuid));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void addChild(MindNode cn) throws java.sql.SQLException {
        Set<String> cuuids = new HashSet<>(Arrays.asList(getChildrenUuids()));
        cuuids.add(cn.getUuid());
        setSysProp(CHILD_K, String.join(",", cuuids));
    }

    public void removeChild(Node cn) throws java.sql.SQLException {
        Set<String> cuuids = new HashSet<>(Arrays.asList(getChildrenUuids()));
        cuuids.remove(cn.getUuid());
        setSysProp(CHILD_K, String.join(",", cuuids));
    }

    public void removeAllChild() throws java.sql.SQLException {
        setSysProp(CHILD_K, "");
    }

    public void walk(Consumer<MindNode> callback) throws SQLException {
        callback.accept(this);
        for (MindNode child : getChildren()) {
            child.walk(callback);
        }
    }

    private final static String KEY_X = "mindmap.node.x";
    private final static String KEY_Y = "mindmap.node.y";
    private final static String KEY_W = "mindmap.node.w";
    private final static String KEY_H = "mindmap.node.h";

    public double getX() {
        try {
            return getSysPropDouble(KEY_X, 0.0);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;

    }

    public void setX(double x) {
        try {
            setSysPropDouble(KEY_X, x);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getY() {
        try {
            return getSysPropDouble(KEY_Y, 0.0);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    public void setY(double y) {
        try {
            setSysPropDouble(KEY_Y, y);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getW() {
        try {
            return getSysPropDouble(KEY_W, 0.0);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;

    }

    public void setW(double x) {
        try {
            setSysPropDouble(KEY_W, x);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getH() {
        try {
            return getSysPropDouble(KEY_H, 0.0);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    public void setH(double y) {
        try {
            setSysPropDouble(KEY_H, y);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void translate(double dx, double dy) {
        try {
            walk(node -> {
                node.setX(node.getX() + dx);
                node.setY(node.getY() + dy);
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Rectangle getBounds() {
        Rectangle result = new Rectangle();
        result.x = (int) getX();
        result.y = (int) getY();
        result.width = (int) getW();
        result.height = (int) getH();
        return result;
    }

    public void mirrorHorizontal() {
        Rectangle bb = getBounds();
        try {
            walk(node -> {
                // node.x = bb.left + bb.right - node.x - node.width;
                node.setX(bb.getMinX() + bb.getMaxX() - node.getX() - node.getW());
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void mirrorVertical() {
        Rectangle bb = getBounds();
        try {
            walk(node -> {
                // node.y = bb.top + bb.bottom - node.y - node.height;
                node.setY(bb.getMinY() + bb.getMaxY() - node.getY() - node.getH());
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setChildren(List<MindNode> children) {
        try {
            removeAllChild();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (MindNode child : children) {
            try {
                addChild(child);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private final static String KEY_P = "mindmap.node.prelim";
    private final static String KEY_M = "mindmap.node.modifier";

    public double getPrelim() {
        try {
            return getSysPropDouble(KEY_P, 0.0);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    public void setPrelim(double prelim) {
        try {
            setSysPropDouble(KEY_P, prelim);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getModifier() {
        try {
            return getSysPropDouble(KEY_M, 0.0);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    public void setModifier(double modifier) {
        try {
            setSysPropDouble(KEY_M, modifier);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private final static String KEY_R = "mindmap.node.x";

    public void setRoot() {
        try {
            setSysPropDouble(KEY_R, 1.0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isRoot() {
        try {
            return (getSysPropDouble(KEY_R, 0.0) > 0.0);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

}
