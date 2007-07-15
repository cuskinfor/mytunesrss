/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.NotEmptyTextFieldValidation
 */
public class NotEmptyTextFieldValidation extends JTextFieldValidation {

    public NotEmptyTextFieldValidation(JTextField textField) {
        super(textField);
    }

    public NotEmptyTextFieldValidation(JTextField textField, String validationFailedMessage) {
        super(textField, validationFailedMessage);
    }

    protected boolean isValid(String text) {
        return text != null && text.length() > 0;
    }
}