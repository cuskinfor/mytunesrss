/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.swing;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.NotEmptyTextFieldValidation
 */
public class MaxLengthTextFieldValidation extends JTextFieldValidation {
    private int myMaxLength;

    public MaxLengthTextFieldValidation(JTextField textField, int maxLength) {
        super(textField);
        myMaxLength = maxLength;
    }

    public MaxLengthTextFieldValidation(JTextField textField, int maxLength, String validationFailedMessage) {
        super(textField, validationFailedMessage);
        myMaxLength = maxLength;
    }

    protected boolean isValid(String text) {
        return StringUtils.isEmpty(text) || text.length() <= myMaxLength;
    }
}
