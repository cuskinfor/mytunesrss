/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin.validation;

import com.vaadin.data.validator.AbstractStringValidator;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.regex.Pattern;

public class FileValidator extends AbstractStringValidator {

    private boolean myAllowFile;
    private boolean myAllowDirectory;
    private Pattern myNamePattern;

    public FileValidator(String errorMessage, boolean allowFile, boolean allowDirectory, Pattern namePattern) {
        super(errorMessage);
        myAllowFile = allowFile;
        myAllowDirectory = allowDirectory;
        myNamePattern = namePattern;
    }

    @Override
    protected boolean isValidString(String value) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        File file = new File(value);
        if (myAllowFile && file.isFile() || myAllowDirectory && file.isDirectory()) {
            return myNamePattern == null || myNamePattern.matcher(file.getName()).matches();
        }
        return false;
    }
}
