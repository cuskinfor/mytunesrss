/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin.validation;

import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.ui.AbstractField;

public class SameValidator extends AbstractValidator {

    private AbstractField myOtherField;

    public SameValidator(AbstractField otherField, String errorMessage) {
        super(errorMessage);
        myOtherField = otherField;
    }

    public boolean isValid(Object o) {
        Object otherValue = myOtherField.getValue();
        return otherValue == o || o != null && o.equals(otherValue);
    }
}
