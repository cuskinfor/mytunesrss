/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin.validation;

import com.vaadin.data.validator.AbstractStringValidator;
import de.codewave.mytunesrss.MyTunesRssUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;

public class VlcExecutableFileValidator extends AbstractStringValidator {

    public VlcExecutableFileValidator(String errorMessage) {
        super(errorMessage);
    }

    @Override
    protected boolean isValidString(String value) {
        if (value != null) {
            File file = null;
            file = new File(value);
            if (file.isDirectory() && SystemUtils.IS_OS_MAC_OSX && "vlc.app".equalsIgnoreCase(file.getName())) {
                file = new File(file, "Contents/MacOS/VLC");
            }
            return MyTunesRssUtils.canExecute(file);
        }
        return true;
    }
}
