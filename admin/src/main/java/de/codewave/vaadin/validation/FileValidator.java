/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin.validation;

import com.vaadin.data.validator.AbstractStringValidator;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.regex.Pattern;

public class FileValidator extends AbstractStringValidator {

    public static final Pattern PATTERN_ALL = Pattern.compile("^.*$");

    private Pattern myAllowedFilePattern;
    private Pattern myAllowedDirPattern;

    public FileValidator(String errorMessage, Pattern allowedDirPattern, Pattern allowedFilePattern) {
        super(errorMessage);
        myAllowedDirPattern = allowedDirPattern;
        myAllowedFilePattern = allowedFilePattern;
    }

    @Override
    protected boolean isValidString(String value) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        File file = new File(value);
        return (file.isFile() && myAllowedFilePattern != null && myAllowedFilePattern.matcher(value).matches()) || (file.isDirectory() && myAllowedDirPattern != null && myAllowedDirPattern.matcher(value).matches());
    }
}
