/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import de.codewave.vaadin.validation.EmailValidator;
import de.codewave.vaadin.validation.MinMaxIntegerValidator;

import java.util.ResourceBundle;

public class ValidatorFactory {

    private ResourceBundle myBundle;

    public ValidatorFactory(ResourceBundle bundle) {
        myBundle = bundle;
    }

    public MinMaxIntegerValidator createPortValidator() {
        return new MinMaxIntegerValidator(MyTunesRssWebAdmin.getBundleString(myBundle, "error.minMaxValue", 1, 65535), 1, 65535);
    }

    public MinMaxIntegerValidator createMinMaxValidator(int minValue, int maxValue) {
        return new MinMaxIntegerValidator(MyTunesRssWebAdmin.getBundleString(myBundle, "error.minMaxValue", minValue, maxValue), minValue, maxValue);
    }

    public EmailValidator createEmailValidator() {
        return new EmailValidator(MyTunesRssWebAdmin.getBundleString(myBundle, "error.invalidEmail"));
    }
}
