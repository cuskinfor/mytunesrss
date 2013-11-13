/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin.validation;

import com.vaadin.data.validator.AbstractStringValidator;
import de.codewave.mytunesrss.MyTunesRssUtils;

import java.io.File;

public class GraphicsMagickExecutableFileValidator extends AbstractStringValidator {

    public GraphicsMagickExecutableFileValidator(String errorMessage) {
        super(errorMessage);
    }

    @Override
    protected boolean isValidString(String value) {
        if (value != null) {
            File file = null;
            file = new File(value);
            return MyTunesRssUtils.isExecutable(file);
        }
        return true;
    }
}
