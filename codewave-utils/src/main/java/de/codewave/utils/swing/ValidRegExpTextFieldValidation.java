/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.swing;

import javax.swing.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * de.codewave.mytunesrss.NotEmptyTextFieldValidation
 */
public class ValidRegExpTextFieldValidation extends JTextFieldValidation {

    public ValidRegExpTextFieldValidation(JTextField textField) {
        super(textField);
    }

    public ValidRegExpTextFieldValidation(JTextField textField, String validationFailedMessage) {
        super(textField, validationFailedMessage);
    }

    protected boolean isValid(String text) {
        if (text != null && text.length() > 0) {
            try {
                Pattern.compile(text);
            } catch (PatternSyntaxException e) {
                return false;
            }
        }
        return true;
    }
}