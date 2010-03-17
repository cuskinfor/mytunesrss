/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import de.codewave.vaadin.validation.EmailValidator;
import de.codewave.vaadin.validation.MinMaxIntegerValidator;

public class ValidatorFactory {

    public static MinMaxIntegerValidator createPortValidator() {
        return new MinMaxIntegerValidator(MyTunesRssWebAdminUtils.getBundleString("error.minMaxValue", 1, 65535), 1, 65535);
    }

    public static MinMaxIntegerValidator createMinMaxValidator(int minValue, int maxValue) {
        return new MinMaxIntegerValidator(MyTunesRssWebAdminUtils.getBundleString("error.minMaxValue", minValue, maxValue), minValue, maxValue);
    }

    public static EmailValidator createEmailValidator() {
        return new EmailValidator(MyTunesRssWebAdminUtils.getBundleString("error.invalidEmail"));
    }
}
