/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.swing;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.beans.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.JTextFieldValidation
 */
public abstract class JTextFieldValidation implements DocumentListener, PropertyChangeListener {
    private static Map<JTextField, JTextFieldValidation> LISTENERS = new HashMap<JTextField, JTextFieldValidation>();

    public static void setValidation(JTextFieldValidation validation) {
        synchronized (validation.getTextField()) {
            removeValidation(validation.getTextField());
            validation.getTextField().getDocument().addDocumentListener(validation);
            validation.getTextField().addPropertyChangeListener(validation);
            LISTENERS.put(validation.getTextField(), validation);
        }
    }

    public static void removeValidation(JTextField textField) {
        JTextFieldValidation listener = LISTENERS.remove(textField);
        if (listener != null) {
            textField.getDocument().removeDocumentListener(listener);
        }
    }

    public static boolean validate(JTextField... textFields) {
        boolean valid = true;
        if (textFields != null && textFields.length > 0) {
            for (JTextField textField : textFields) {
                JTextFieldValidation validation = LISTENERS.get(textField);
                if (validation != null && !validation.isValid()) {
                    valid = false;
                }
            }
        }
        return valid;
    }

    public static boolean validateAll(JComponent component) {
        boolean valid = true;
        if (component instanceof JTextField) {
            if (!validate((JTextField)component)) {
                valid = false;
            }
        }
        Component[] childComponents = component.getComponents();
        for (int i = 0; i < childComponents.length; i++) {
            if (childComponents[i] instanceof JComponent) {
                if (!validateAll((JComponent)childComponents[i])) {
                    valid = false;
                }
            }
        }
        return valid;
    }

    public static String getValidationFailureMessage(JTextField... textFields) {
        StringBuffer messages = new StringBuffer();
        if (textFields != null && textFields.length > 0) {
            for (JTextField textField : textFields) {
                JTextFieldValidation validation = LISTENERS.get(textField);
                if (validation != null && validation.getValidationFailedMessage() != null && validation.getValidationFailedMessage().length() > 0 &&
                        !validation.isValid()) {
                    messages.append(validation.getValidationFailedMessage()).append(" ");
                }
            }
        }
        return messages.toString().trim();
    }

    public static String getAllValidationFailureMessage(JComponent component) {
        StringBuffer messages = new StringBuffer();
        if (component instanceof JTextField) {
            JTextFieldValidation validation = LISTENERS.get(component);
            if (validation != null && validation.getValidationFailedMessage() != null && validation.getValidationFailedMessage().length() > 0 &&
                    !validation.isValid()) {
                messages.append(validation.getValidationFailedMessage()).append(" ");
            }
        }
        Component[] childComponents = component.getComponents();
        for (int i = 0; i < childComponents.length; i++) {
            if (childComponents[i] instanceof JComponent) {
                String appendValue = getAllValidationFailureMessage((JComponent)childComponents[i]);
                if (appendValue != null) {
                    messages.append(appendValue).append(" ");
                }
            }
        }
        String returnValue = messages.toString().trim();
        return returnValue.length() > 0 ? returnValue : null;
    }

    private Color myValidColor;
    private final Color myInvalidColor = new Color(255, 200, 200);
    private JTextField myTextField;
    private String myValidationFailedMessage;

    protected JTextFieldValidation(JTextField textField) {
        myTextField = textField;
        myValidColor = textField.getBackground();
    }

    protected JTextFieldValidation(JTextField textField, String validationFailedMessage) {
        myTextField = textField;
        myValidColor = textField.getBackground();
        myValidationFailedMessage = validationFailedMessage;
    }

    String getValidationFailedMessage() {
        return myValidationFailedMessage;
    }

    protected JTextField getTextField() {
        return myTextField;
    }

    public final boolean isValid() {
        if (myTextField.isEnabled()) {
            if (!isValid(myTextField.getText())) {
                markInvalid();
                return false;
            }
        }
        markValid();
        return true;
    }

    protected abstract boolean isValid(String text);

    protected void markValid() {
        myTextField.setBackground(myValidColor);
    }

    protected void markInvalid() {
        myTextField.setBackground(myInvalidColor);
    }

    public final void changedUpdate(DocumentEvent e) {
        // intentionally left blank
    }

    public final void insertUpdate(DocumentEvent e) {
        isValid();
    }

    public final void removeUpdate(DocumentEvent e) {
        isValid();
    }

    public final void propertyChange(PropertyChangeEvent evt) {
        if ("enabled".equals(evt.getPropertyName())) {
            isValid();
        }
    }
}