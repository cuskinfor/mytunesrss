package de.codewave.mytunesrss;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * de.codewave.mytunesrss.CompiledPathReplacement
 */
public class CompiledPathReplacement {
    private Pattern myPattern;
    private String myReplacement;

    public CompiledPathReplacement(PathReplacement pathReplacement) {
        myPattern = Pattern.compile(pathReplacement.getSearchPattern());
        myReplacement = pathReplacement.getReplacement();
    }

    public String replace(String input) {
        Matcher matcher = myPattern.matcher(input);
        return matcher.replaceAll(myReplacement);
    }
}