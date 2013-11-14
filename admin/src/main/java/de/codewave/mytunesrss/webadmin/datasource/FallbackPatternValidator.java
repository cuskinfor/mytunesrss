/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.datasource;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.vaadin.validation.ValidRegExpValidator;
import org.apache.commons.lang3.StringUtils;

public class FallbackPatternValidator extends ValidRegExpValidator {

    public FallbackPatternValidator(String errorMessage) {
        super(errorMessage);
    }

    @Override
    protected boolean isValidString(String value) {
        String[] dirTokens = MyTunesRssUtils.substringsBetween(value, "[[[dir:", "]]]");
        for (String dirToken : dirTokens) {
            String[] numberAndRegExp = StringUtils.split(StringUtils.trimToEmpty(dirToken), ":", 2);
            if (numberAndRegExp.length < 1 || numberAndRegExp.length > 2) {
                return false;
            }
            if (!StringUtils.isNumeric(StringUtils.trim(numberAndRegExp[0]))) {
                return false;
            }
            if (numberAndRegExp.length > 1 && !super.isValidString(numberAndRegExp[1])) {
                return false;
            }
        }
        String[] fileTokens = MyTunesRssUtils.substringsBetween(value, "[[[file", "]]]");
        for (String fileToken : fileTokens) {
            if (fileToken.startsWith(":")) {
                if (StringUtils.isBlank(fileToken.substring(1)) || !super.isValidString(StringUtils.trim(fileToken.substring(1)))) {
                    return false;
                }
            }
        }
        return true;
    }
}
