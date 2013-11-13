/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import de.codewave.vaadin.validation.EmailValidator;
import de.codewave.vaadin.validation.MinMaxIntegerValidator;

import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

public class ValidatorFactory implements Serializable {

    private static final long serialVersionUID = 1;
    
    private String myBundleName;
    private Locale myLocale;
    private transient ResourceBundle myBundle;

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        myBundle = MyTunesRssWebAdmin.RESOURCE_BUNDLE_MANAGER.getBundle(myBundleName, myLocale);
    }

    public ValidatorFactory(String bundleName, Locale locale) {
        myBundleName = bundleName;
        myLocale = locale;
        myBundle = MyTunesRssWebAdmin.RESOURCE_BUNDLE_MANAGER.getBundle(bundleName, locale);
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
