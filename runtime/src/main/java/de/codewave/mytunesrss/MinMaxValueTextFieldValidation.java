/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import javax.swing.*;
import java.math.*;

/**
 * de.codewave.mytunesrss.MinMaxValueTextFieldValidation
 */
public class MinMaxValueTextFieldValidation extends JTextFieldValidation {
    private BigDecimal myMinValue;
    private BigDecimal myMaxValue;
    private boolean myAllowEmpty;

    public MinMaxValueTextFieldValidation(JTextField textField, long minValue, long maxValue, boolean allowEmpty) {
        super(textField);
        myMinValue = new BigDecimal(minValue);
        myMaxValue = new BigDecimal(maxValue);
        myAllowEmpty = allowEmpty;
    }

    public MinMaxValueTextFieldValidation(JTextField textField, long minValue, long maxValue, boolean allowEmpty, String validationFailedMessage) {
        super(textField, validationFailedMessage);
        myMinValue = new BigDecimal(minValue);
        myMaxValue = new BigDecimal(maxValue);
        myAllowEmpty = allowEmpty;
    }

    protected boolean isValid(String text) {
        if (myAllowEmpty && (text == null || text.trim().length() == 0)) {
            return true;
        }
        try {
            BigDecimal value = new BigDecimal(text);
            return value.compareTo(myMinValue) >= 0 && value.compareTo(myMaxValue) <= 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}