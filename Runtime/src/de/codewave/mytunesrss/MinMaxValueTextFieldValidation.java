/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.MinMaxValueTextFieldValidation
 */
public class MinMaxValueTextFieldValidation extends JTextFieldValidation {
    private long myMinValue;
    private long myMaxValue;
    private boolean myAllowEmpty;

    public MinMaxValueTextFieldValidation(JTextField textField, long minValue, long maxValue, boolean allowEmpty) {
        super(textField);
        myMinValue = minValue;
        myMaxValue = maxValue;
        myAllowEmpty = allowEmpty;
    }

    public MinMaxValueTextFieldValidation(JTextField textField, long minValue, long maxValue, boolean allowEmpty, String validationFailedMessage) {
        super(textField, validationFailedMessage);
        myMinValue = minValue;
        myMaxValue = maxValue;
        myAllowEmpty = allowEmpty;
    }

    protected boolean isValid(String text) {
        if (myAllowEmpty && (text == null || text.trim().length() == 0)) {
            return true;
        }
        try {
            long value = Integer.parseInt(text);
            return value >= myMinValue && value <= myMaxValue;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}