/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin.validation;

import com.vaadin.data.validator.AbstractStringValidator;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ValidRegExpValidator extends AbstractStringValidator {

    public ValidRegExpValidator(String errorMessage) {
        super(errorMessage);
    }

    @Override
    protected boolean isValidString(String value) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        try {
            Pattern.compile(value);
        } catch (PatternSyntaxException e) {
            return false;
        }
        return true;
    }
}
