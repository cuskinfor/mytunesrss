/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.datasource;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.vaadin.validation.ValidRegExpValidator;
import org.apache.commons.lang.StringUtils;

public class FallbackPatternValidator extends ValidRegExpValidator {

    public FallbackPatternValidator(String errorMessage) {
        super(errorMessage);
    }

    @Override
    protected boolean isValidString(String value) {
        String[] dirTokens = MyTunesRssUtils.substringsBetween(value, "[[[dir:", "]]]");
        for (String dirToken : dirTokens) {
            String[] numberAndRegExp = StringUtils.split(StringUtils.trimToEmpty(dirToken), ":", 2);
            if (numberAndRegExp.length != 2) {
                return false;
            }
            if (!super.isValidString(numberAndRegExp[1])) {
                return false;
            }
        }
        String[] fileTokens = MyTunesRssUtils.substringsBetween(value, "[[[file", "]]]");
        for (String fileToken : fileTokens) {
            if (!super.isValidString(fileToken)) {
                return false;
            }
        }
        return true;
    }
}
