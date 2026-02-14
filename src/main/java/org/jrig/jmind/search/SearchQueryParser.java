/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.search;

public class SearchQueryParser {

    /**
     * Converts Google-like query to FTS5 syntax
     * Examples:
     * - "hello world" -> hello AND world
     * - "hello OR world" -> hello OR world
     * - "hello -world" -> hello NOT world
     * - "\"exact phrase\"" -> "exact phrase"
     */
    public String parseQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "";
        }

        StringBuilder ftsQuery = new StringBuilder();
        String[] tokens = tokenizeQuery(query);

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];

            if (token.equals("OR")) {
                ftsQuery.append(" OR ");
            } else if (token.startsWith("-")) {
                // Exclude term
                ftsQuery.append(" NOT ").append(token.substring(1));
            } else if (token.startsWith("\"") && token.endsWith("\"")) {
                // Exact phrase
                ftsQuery.append(token);
            } else {
                // Regular term
                if (i > 0 && !ftsQuery.toString().endsWith(" OR ")
                        && !ftsQuery.toString().endsWith(" NOT ")) {
                    ftsQuery.append(" AND ");
                }
                ftsQuery.append(token).append("*"); // Prefix matching
            }
        }

        return ftsQuery.toString().trim();
    }

    private String[] tokenizeQuery(String query) {
        java.util.List<String> tokens = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (c == ' ' && !inQuotes) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        return tokens.toArray(new String[0]);
    }
}