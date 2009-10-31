package de.codewave.mytunesrss;

import java.util.regex.Pattern;

/**
 * de.codewave.mytunesrss.PathReplacement
 */
public class PathReplacement {
    private String mySearchPattern;
    private String myReplacement;

    public PathReplacement(String searchPattern, String replacement) {
        mySearchPattern = searchPattern;
        myReplacement = replacement;
    }

    public String getSearchPattern() {
        return mySearchPattern;
    }

    public void setSearchPattern(String searchPattern) {
        mySearchPattern = searchPattern;
    }

    public String getReplacement() {
        return myReplacement;
    }

    public void setReplacement(String replacement) {
        myReplacement = replacement;
    }
}