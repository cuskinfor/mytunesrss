package de.codewave.mytunesrss;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.CheckboxCheckedTextFieldValidation
 */
public class CheckboxCheckedTextFieldValidation extends JTextFieldValidation {
    private JCheckBox myCheckBox;
    private JTextFieldValidation myValidation;

    public CheckboxCheckedTextFieldValidation(JTextField textField, JCheckBox checkBox, JTextFieldValidation validation) {
        super(textField);
        myCheckBox = checkBox;
        myValidation = validation;
    }

    public CheckboxCheckedTextFieldValidation(JTextField textField, JCheckBox checkBox, JTextFieldValidation validation, String validationFailedMessage) {
        super(textField, validationFailedMessage);
        myCheckBox = checkBox;
        myValidation = validation;
    }

    protected boolean isValid(String text) {
        return !myCheckBox.isSelected() || myValidation.isValid(text);
    }
}