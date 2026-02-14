/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.model;

import java.sql.Connection;

public class BlogNode extends Node {

    public BlogNode(Connection conn) {
        super(conn);
    }

    public BlogNode(Connection conn, String uuid) {
        super(conn, uuid);
    }

}
