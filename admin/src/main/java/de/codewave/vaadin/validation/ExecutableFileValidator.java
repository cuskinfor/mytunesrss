/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin.validation;

import com.vaadin.data.validator.AbstractStringValidator;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.regex.Pattern;

public class ExecutableFileValidator extends FileValidator {

    public ExecutableFileValidator(String errorMessage, Pattern allowedDirPattern, Pattern allowedFilePattern) {
        super(errorMessage, allowedDirPattern, allowedFilePattern);
    }

    @Override
    protected boolean isValidString(String value) {
        return super.isValidString(value) && new File(value).canExecute();
    }
}
