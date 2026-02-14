/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.model;

public class SearchResult {

    public String uuid;
    public double rank;
    public String snippet;

    public SearchResult(String uuid, double rank, String snippet) {
        this.uuid = uuid;
        this.rank = rank;
        this.snippet = snippet;
    }

}
