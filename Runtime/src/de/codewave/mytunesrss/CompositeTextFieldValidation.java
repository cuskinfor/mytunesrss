/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import org.apache.commons.lang.*;

import javax.swing.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.NotEmptyTextFieldValidation
 */
public class CompositeTextFieldValidation extends JTextFieldValidation {
    private JTextFieldValidation[] myValidations;

    public CompositeTextFieldValidation(JTextField textField, JTextFieldValidation... validations) {
        super(textField);
        myValidations = validations;
    }

    protected boolean isValid(String text) {
        for (JTextFieldValidation validation : myValidations) {
            if (!validation.isValid(text)) {
                return false;
            }
        }
        return true;
    }


    @Override
    String getValidationFailedMessage() {
        StringBuffer buffer = new StringBuffer();
        for (JTextFieldValidation validation : myValidations) {
            if (!validation.isValid()) {
                buffer.append(validation.getValidationFailedMessage()).append(" ");
            }
        }
        return buffer.toString().trim();
    }
}