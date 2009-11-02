package de.codewave.mytunesrss;

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

    @Override
    public int hashCode() {
        return mySearchPattern.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof PathReplacement && mySearchPattern.equals(((PathReplacement) obj).getSearchPattern());
    }
}