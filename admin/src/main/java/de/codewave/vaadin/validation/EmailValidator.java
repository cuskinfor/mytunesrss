/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin.validation;

import com.vaadin.data.validator.AbstractStringValidator;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Validator for email addresses. Allows either an address only (e.g. john.doe@mail.com) or
 * a name and an address (e.g. John Doe <john.doe@mail.com>).
 */
public class EmailValidator extends AbstractStringValidator {
    /**
     * Create a new validator with an error message.
     *
     * @param errorMessage An error message.
     */
    public EmailValidator(String errorMessage) {
        super(errorMessage);
    }

    @Override
    protected boolean isValidString(String s) {
        try {
            new InternetAddress(s, true);
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
