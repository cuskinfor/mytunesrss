package de.codewave.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Wildcard matcher which matches strings against a pattern containing any number of wildcards. The only supported wildcard is
 * the "*" character for matching any number of characters. The backslash is used as an escape character to match the "*" itself.
 * Escaping any other character than the "*" has no effect and is the same as not escaping it.
 */
public class WildcardMatcher {

    private List<String> myParts = new ArrayList<String>();
    private boolean myStartsWithWildcard;
    private boolean myEndsWithWildcard;

    public WildcardMatcher(String pattern) {
        if (StringUtils.isBlank(pattern)) {
            throw new IllegalArgumentException("Pattern must not be blank.");
        }
        myStartsWithWildcard = pattern.startsWith("*");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '\\') {
                // next character is escaped
                if (i == pattern.length() - 1) {
                    throw new IllegalArgumentException("Pattern \"" + pattern + "\" ends with the escape character!");
                }
                i++;
                builder.append(pattern.charAt(i));
            } else if (pattern.charAt(i) == '*') {
                // wildcard, start a new part
                myParts.add(builder.toString());
                builder.delete(0, builder.length());
                if (i == pattern.length() - 1) {
                    myEndsWithWildcard = true;
                }
            } else {
                builder.append(pattern.charAt(i));
            }
        }
        if (builder.length() > 0) {
            myParts.add(builder.toString());
        }
    }

    public boolean matches(String test) {
        if (myParts.isEmpty()) {
            return true;
        }
        int startIndex = 0;
        for (int i = 0; i < myParts.size(); i++) {
            String currentPart = myParts.get(i);
            if (i == 0 && !myStartsWithWildcard) {
                if (!test.startsWith(currentPart)) {
                    return false;
                } else {
                    startIndex = currentPart.length();
                }
            } else if (i == myParts.size() - 1 && !myEndsWithWildcard) {
                return test.endsWith(currentPart);
            } else {
                int matchIndex = test.indexOf(currentPart, startIndex);
                if (matchIndex == -1) {
                    return false;
                } else {
                    startIndex = matchIndex + currentPart.length();
                }
            }
        }
        return true;
    }
}
