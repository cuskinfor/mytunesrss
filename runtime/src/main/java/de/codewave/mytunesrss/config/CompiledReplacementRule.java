package de.codewave.mytunesrss.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * de.codewave.mytunesrss.config.CompiledReplacementRule
 */
public class CompiledReplacementRule {
    private Pattern myPattern;
    private String myReplacement;

    public CompiledReplacementRule(ReplacementRule replacementRule) {
        myPattern = Pattern.compile(replacementRule.getSearchPattern());
        myReplacement = replacementRule.getReplacement();
    }

    public boolean matches(String input) {
        return myPattern.matcher(input).find();
    }

    public String replace(String input) {
        Matcher matcher = myPattern.matcher(input);
        return matcher.replaceAll(myReplacement);
    }
}